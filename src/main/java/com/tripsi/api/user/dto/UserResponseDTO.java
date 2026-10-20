package com.tripsi.api.user.dto;

import com.tripsi.api.user.domain.Role;
import com.tripsi.api.user.domain.Status;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String name,
        String email,
        Set<Role> roles,
        Status status,
        Instant createdAt
) {
}
