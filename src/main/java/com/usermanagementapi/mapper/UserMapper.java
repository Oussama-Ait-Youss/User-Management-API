package com.usermanagementapi.mapper;

import com.usermanagementapi.dto.UserCreateRequest;
import com.usermanagementapi.dto.UserResponse;
import com.usermanagementapi.dto.UserUpdateRequest;
import com.usermanagementapi.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(UserCreateRequest request, String encodedPassword) {
        return new User(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                encodedPassword,
                request.getRole()
        );
    }

    public void updateEntityFromDto(UserUpdateRequest request, User user) {
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getEmail(),
                user.getPassword(),
                user.isActive()
        );
    }
}