package com.tripsi.api.user.adapter.in.rest;

import com.tripsi.api.user.dto.AuthResponseDTO;
import com.tripsi.api.user.dto.LoginRequestDTO;
import com.tripsi.api.user.dto.RegisterRequestDTO;
import com.tripsi.api.user.dto.UserResponseDTO;
import com.tripsi.api.user.port.in.AuthServicePort;
import com.tripsi.api.user.port.in.UserServicePort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "Registration and login")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserServicePort userServicePort;
    private final AuthServicePort authServicePort;

    public AuthController(UserServicePort userServicePort, AuthServicePort authServicePort) {
        this.userServicePort = userServicePort;
        this.authServicePort = authServicePort;
    }

    @Operation(summary = "Register a new user")
    @ApiResponse(responseCode = "201", description = "User created")
    @ApiResponse(responseCode = "422", description = "Email already registered")
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userServicePort.register(request));
    }

    @Operation(summary = "Authenticate and receive a JWT access token")
    @ApiResponse(responseCode = "200", description = "Authenticated")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authServicePort.login(request));
    }
}
