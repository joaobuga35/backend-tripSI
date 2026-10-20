package com.tripsi.api.user.adapter.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripsi.api.shared.security.JwtTokenProvider;
import com.tripsi.api.user.dto.LoginRequestDTO;
import com.tripsi.api.user.dto.RegisterRequestDTO;
import com.tripsi.api.user.dto.UpdateUserDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class AuthAndUserControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${tripsi.jwt.secret}")
    private String jwtSecret;

    @Test
    void should_register_user_and_return_201_without_exposing_password() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO("Jane Doe", "jane.register@tripsi.com", "plain-password");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("jane.register@tripsi.com"))
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void should_return_422_when_registering_with_already_used_email() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO("Jane Doe", "jane.duplicate@tripsi.com", "plain-password");
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("Email already registered: jane.duplicate@tripsi.com"));
    }

    @Test
    void should_return_400_with_field_errors_when_register_payload_is_invalid() throws Exception {
        RegisterRequestDTO invalidRequest = new RegisterRequestDTO("", "not-an-email", "short");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void should_login_and_return_access_token_when_credentials_are_valid() throws Exception {
        registerUser("jane.login@tripsi.com", "plain-password");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequestDTO("jane.login@tripsi.com", "plain-password"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").isNumber());
    }

    @Test
    void should_return_401_with_generic_detail_when_login_password_is_wrong() throws Exception {
        registerUser("jane.wrongpass@tripsi.com", "plain-password");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequestDTO("jane.wrongpass@tripsi.com", "wrong-password"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid credentials"));
    }

    @Test
    void should_return_401_when_login_with_unknown_email() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequestDTO("unknown@tripsi.com", "plain-password"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid credentials"));
    }

    @Test
    void should_return_401_when_calling_protected_endpoint_without_token() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Authentication required"));
    }

    @Test
    void should_return_401_when_token_is_malformed() throws Exception {
        mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void should_return_401_when_token_is_expired() throws Exception {
        JwtTokenProvider expiredTokenProvider = new JwtTokenProvider(jwtSecret, -1_000L);
        String expiredToken = expiredTokenProvider.generateToken(UUID.randomUUID(), "expired@tripsi.com", Set.of("ROLE_USER"));

        mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Authentication required"));
    }

    @Test
    void should_get_current_user_when_authenticated() throws Exception {
        registerUser("jane.me@tripsi.com", "plain-password");
        String token = loginAndGetToken("jane.me@tripsi.com", "plain-password");

        mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jane.me@tripsi.com"));
    }

    @Test
    void should_update_current_user_profile() throws Exception {
        registerUser("jane.update@tripsi.com", "plain-password");
        String token = loginAndGetToken("jane.update@tripsi.com", "plain-password");

        mockMvc.perform(put("/api/v1/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateUserDTO("Jane Updated"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Updated"));
    }

    @Test
    void should_soft_delete_current_user() throws Exception {
        registerUser("jane.delete@tripsi.com", "plain-password");
        String token = loginAndGetToken("jane.delete@tripsi.com", "plain-password");

        mockMvc.perform(delete("/api/v1/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequestDTO("jane.delete@tripsi.com", "plain-password"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid credentials"));
    }

    private void registerUser(String email, String password) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RegisterRequestDTO("Jane Doe", email, password))));
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequestDTO(email, password))))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(responseBody).get("accessToken").asText();
    }
}
