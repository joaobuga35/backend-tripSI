package com.tripsi.api.user.port.in;

import com.tripsi.api.user.dto.AuthResponseDTO;
import com.tripsi.api.user.dto.LoginRequestDTO;

// Primary port for the login flow — kept separate from UserServicePort since authentication
// (AuthenticationManager + JWT issuance) is a distinct concern from user CRUD.
public interface AuthServicePort {

    AuthResponseDTO login(LoginRequestDTO request);
}
