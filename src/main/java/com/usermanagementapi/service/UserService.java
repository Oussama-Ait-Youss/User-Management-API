package com.usermanagementapi.service;

import com.usermanagementapi.dto.request.ChangePasswordRequest;
import com.usermanagementapi.dto.request.UserCreateRequest;
import com.usermanagementapi.dto.request.UserUpdateRequest;
import com.usermanagementapi.dto.response.UserResponse;
import com.usermanagementapi.model.Role;

import java.util.List;

public interface UserService {

    UserResponse createUser(UserCreateRequest request);

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long id);

    UserResponse updateUser(Long id, UserUpdateRequest request);

    void deactivateUser(Long id);

    UserResponse changePassword(Long id, ChangePasswordRequest request);

    List<UserResponse> getUsersByRole(Role role);
}
