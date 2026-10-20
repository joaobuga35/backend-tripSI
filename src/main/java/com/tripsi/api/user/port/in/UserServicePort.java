package com.tripsi.api.user.port.in;

import com.tripsi.api.user.dto.RegisterRequestDTO;
import com.tripsi.api.user.dto.UpdateUserDTO;
import com.tripsi.api.user.dto.UserResponseDTO;

import java.util.UUID;

// Primary port — controllers/event listeners depend on this, never on UserService directly.
public interface UserServicePort {

    UserResponseDTO register(RegisterRequestDTO request);

    UserResponseDTO findById(UUID id);

    UserResponseDTO findByEmail(String email);

    UserResponseDTO update(UUID id, UpdateUserDTO request);

    void softDelete(UUID id);
}
