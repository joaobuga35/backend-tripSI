package com.tripsi.api.user.adapter.in.rest;

import com.tripsi.api.user.dto.UpdateUserDTO;
import com.tripsi.api.user.dto.UserResponseDTO;
import com.tripsi.api.user.port.in.UserServicePort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Users", description = "User profile management")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserServicePort userServicePort;

    public UserController(UserServicePort userServicePort) {
        this.userServicePort = userServicePort;
    }

    @Operation(summary = "Get a user by id (only the authenticated owner)")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "403", description = "Not the resource owner")
    @PreAuthorize("#id == authentication.principal.userId()")
    @GetMapping("/{id}")
    public UserResponseDTO getUser(@PathVariable UUID id) {
        return userServicePort.findById(id);
    }

    @Operation(summary = "Update a user by id (only the authenticated owner)")
    @ApiResponse(responseCode = "200", description = "User updated")
    @ApiResponse(responseCode = "403", description = "Not the resource owner")
    @PreAuthorize("#id == authentication.principal.userId()")
    @PutMapping("/{id}")
    public UserResponseDTO updateUser(@PathVariable UUID id, @Valid @RequestBody UpdateUserDTO request) {
        return userServicePort.update(id, request);
    }

    @Operation(summary = "Soft-delete a user by id (only the authenticated owner)")
    @ApiResponse(responseCode = "204", description = "User soft-deleted")
    @ApiResponse(responseCode = "403", description = "Not the resource owner")
    @PreAuthorize("#id == authentication.principal.userId()")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable UUID id) {
        userServicePort.softDelete(id);
    }
}
