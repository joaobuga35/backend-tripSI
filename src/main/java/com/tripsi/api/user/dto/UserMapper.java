package com.tripsi.api.user.dto;

import com.tripsi.api.user.domain.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDTO toResponse(User user);
}
