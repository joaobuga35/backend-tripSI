package com.tripsi.api.user.application;

import com.tripsi.api.shared.security.JwtTokenProvider;
import com.tripsi.api.user.adapter.in.security.UserPrincipal;
import com.tripsi.api.user.dto.AuthResponseDTO;
import com.tripsi.api.user.dto.LoginRequestDTO;
import com.tripsi.api.user.port.in.AuthServicePort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService implements AuthServicePort {

    private static final String BEARER_TOKEN_TYPE = "Bearer";

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Set<String> roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        String token = jwtTokenProvider.generateToken(principal.getId(), principal.getUsername(), roles);
        return new AuthResponseDTO(token, BEARER_TOKEN_TYPE, jwtTokenProvider.getExpirationSeconds());
    }
}
