package com.tripsi.api.user.adapter.in.rest;

import com.tripsi.api.shared.security.JwtPrincipal;
import com.tripsi.api.user.dto.UpdateUserDTO;
import com.tripsi.api.user.dto.UserResponseDTO;
import com.tripsi.api.user.port.in.UserServicePort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// The user id always comes from the JWT principal, never from client input — no IDOR surface.
@Tag(name = "Users", description = "Current user profile")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserServicePort userServicePort;

    public UserController(UserServicePort userServicePort) {
        this.userServicePort = userServicePort;
    }

    @Operation(summary = "Get the authenticated user's profile")
    @ApiResponse(responseCode = "200", description = "User found")
    @GetMapping("/me")
    public UserResponseDTO getCurrentUser(@AuthenticationPrincipal JwtPrincipal principal) {
        return userServicePort.findById(principal.userId());
    }

    @Operation(summary = "Update the authenticated user's profile")
    @ApiResponse(responseCode = "200", description = "User updated")
    @PutMapping("/me")
    public UserResponseDTO updateCurrentUser(@AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody UpdateUserDTO request) {
        return userServicePort.update(principal.userId(), request);
    }

    @Operation(summary = "Soft-delete the authenticated user's account")
    @ApiResponse(responseCode = "204", description = "User soft-deleted")
    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCurrentUser(@AuthenticationPrincipal JwtPrincipal principal) {
        userServicePort.softDelete(principal.userId());
    }
}
