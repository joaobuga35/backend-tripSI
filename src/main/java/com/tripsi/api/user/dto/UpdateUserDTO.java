package com.tripsi.api.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserDTO(
        @NotBlank String name
) {
}
