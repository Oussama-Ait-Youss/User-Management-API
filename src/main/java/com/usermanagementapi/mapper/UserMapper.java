package com.usermanagementapi.mapper;

import com.usermanagementapi.dto.request.UserCreateRequest;
import com.usermanagementapi.dto.request.UserUpdateRequest;
import com.usermanagementapi.dto.response.UserResponse;
import com.usermanagementapi.model.Role;
import com.usermanagementapi.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(UserCreateRequest request, String encodedPassword) {
        return new User(request.getName(), request.getEmail(), encodedPassword,
                request.getRole() == null ? Role.USER : request.getRole());
    }

    public void updateEntityFromDto(UserUpdateRequest request, User user) {
        user.setName(request.getName());
        user.setEmail(request.getEmail());
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(),
                user.getActive(), user.getCreatedAt(), user.getUpdatedAt());
    }
}
