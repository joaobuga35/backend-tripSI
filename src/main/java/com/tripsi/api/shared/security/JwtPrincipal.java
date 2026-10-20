package com.tripsi.api.shared.security;

import java.util.UUID;

// Authentication principal set by JwtAuthenticationFilter — generic, no User/module dependency.
public record JwtPrincipal(UUID userId, String email) {
}
