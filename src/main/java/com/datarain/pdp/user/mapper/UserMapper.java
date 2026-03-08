package com.datarain.pdp.user.mapper;

import com.datarain.pdp.user.dto.UserResponse;
import com.datarain.pdp.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
}
