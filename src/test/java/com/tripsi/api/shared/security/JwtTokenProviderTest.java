package com.tripsi.api.shared.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private static final String TEST_SECRET = base64SecretOfLength(64);

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(TEST_SECRET, 3_600_000L);
    }

    @Test
    void should_generate_token_and_extract_same_claims_back() {
        UUID userId = UUID.randomUUID();
        String email = "jane@tripsi.com";
        Set<String> roles = Set.of("ROLE_USER");

        String token = jwtTokenProvider.generateToken(userId, email, roles);

        assertThat(jwtTokenProvider.isValid(token)).isTrue();
        assertThat(jwtTokenProvider.getUserId(token)).isEqualTo(userId);
        assertThat(jwtTokenProvider.getEmail(token)).isEqualTo(email);
        assertThat(jwtTokenProvider.getRoles(token)).containsExactlyInAnyOrder("ROLE_USER");
    }

    @Test
    void should_expose_configured_expiration_in_seconds() {
        assertThat(jwtTokenProvider.getExpirationSeconds()).isEqualTo(3_600L);
    }

    @Test
    void should_return_false_when_token_is_tampered() {
        String token = jwtTokenProvider.generateToken(UUID.randomUUID(), "jane@tripsi.com", Set.of("ROLE_USER"));
        String tampered = token.substring(0, token.length() - 1) + (token.endsWith("a") ? "b" : "a");

        assertThat(jwtTokenProvider.isValid(tampered)).isFalse();
    }

    @Test
    void should_return_false_when_token_is_malformed() {
        assertThat(jwtTokenProvider.isValid("not-a-jwt")).isFalse();
    }

    @Test
    void should_return_false_when_token_already_expired() {
        JwtTokenProvider expiredTokenProvider = new JwtTokenProvider(TEST_SECRET, -1_000L);
        String token = expiredTokenProvider.generateToken(UUID.randomUUID(), "jane@tripsi.com", Set.of("ROLE_USER"));

        assertThat(expiredTokenProvider.isValid(token)).isFalse();
    }

    private static String base64SecretOfLength(int byteLength) {
        byte[] raw = new byte[byteLength];
        Arrays.fill(raw, (byte) 'a');
        return Base64.getEncoder().encodeToString(raw);
    }
}
