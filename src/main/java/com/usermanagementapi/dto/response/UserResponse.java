package com.usermanagementapi.dto.response;

import com.usermanagementapi.model.Role;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
