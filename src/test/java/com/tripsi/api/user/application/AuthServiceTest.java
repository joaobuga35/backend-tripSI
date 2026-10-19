package com.tripsi.api.user.application;

import com.tripsi.api.user.adapter.in.security.UserPrincipal;
import com.tripsi.api.user.domain.User;
import com.tripsi.api.user.dto.AuthResponseDTO;
import com.tripsi.api.user.dto.LoginRequestDTO;
import com.tripsi.api.shared.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void should_return_access_token_when_credentials_are_valid() {
        User user = User.createNew("Jane Doe", "jane@tripsi.com", "hashed-password");
        UserPrincipal principal = new UserPrincipal(user);
        var authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtTokenProvider.generateToken(eq(user.getId()), eq("jane@tripsi.com"), eq(Set.of("ROLE_USER"))))
                .thenReturn("signed-jwt-token");
        when(jwtTokenProvider.getExpirationSeconds()).thenReturn(3_600L);

        AuthService authService = new AuthService(authenticationManager, jwtTokenProvider);
        AuthResponseDTO response = authService.login(new LoginRequestDTO("jane@tripsi.com", "plain-password"));

        assertThat(response.accessToken()).isEqualTo("signed-jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3_600L);
    }

    @Test
    void should_propagate_bad_credentials_exception_when_authentication_fails() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad credentials"));
        AuthService authService = new AuthService(authenticationManager, jwtTokenProvider);

        assertThatThrownBy(() -> authService.login(new LoginRequestDTO("jane@tripsi.com", "wrong-password")))
                .isInstanceOf(BadCredentialsException.class);
    }
}
