package com.tripsi.api.user.dto;

public record AuthResponseDTO(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
