package com.usermanagementapi.service.impl;

import com.usermanagementapi.dto.request.ChangePasswordRequest;
import com.usermanagementapi.dto.request.UserCreateRequest;
import com.usermanagementapi.dto.request.UserUpdateRequest;
import com.usermanagementapi.dto.response.UserResponse;
import com.usermanagementapi.exception.EmailAlreadyExistsException;
import com.usermanagementapi.exception.InvalidPasswordException;
import com.usermanagementapi.exception.ResourceNotFoundException;
import com.usermanagementapi.mapper.UserMapper;
import com.usermanagementapi.model.Role;
import com.usermanagementapi.model.User;
import com.usermanagementapi.repository.UserRepository;
import com.usermanagementapi.service.UserService;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final int BCRYPT_COST = 12;
    private static final int BCRYPT_MAX_PASSWORD_BYTES = 72;

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserResponse createUser(UserCreateRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email is already registered.");
        }

        String password = request.getPassword();
        validateBcryptPasswordLength(password);
        User user = userMapper.toEntity(request, BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_COST)));
        user.setEmail(email);
        return userMapper.toResponse(saveWithUniqueEmailCheck(user));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAllByActiveTrue().stream().map(userMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return userMapper.toResponse(findActiveUserById(id));
    }

    @Override
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = findActiveUserById(id);
        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmailAndIdNot(email, id)) {
            throw new EmailAlreadyExistsException("Email is already in use by another account.");
        }
        userMapper.updateEntityFromDto(request, user);
        user.setEmail(email);
        try {
            return userMapper.toResponse(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException exception) {
            throw new EmailAlreadyExistsException("Email is already in use by another account.");
        }
    }

    @Override
    public void deactivateUser(Long id) {
        findActiveUserById(id).setActive(false);
    }

    @Override
    public UserResponse changePassword(Long id, ChangePasswordRequest request) {
        User user = findActiveUserById(id);
        validateBcryptPasswordLength(request.getCurrentPassword());
        if (!BCrypt.checkpw(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Current password does not match.");
        }
        validateBcryptPasswordLength(request.getNewPassword());
        user.setPassword(BCrypt.hashpw(request.getNewPassword(), BCrypt.gensalt(BCRYPT_COST)));
        return userMapper.toResponse(userRepository.saveAndFlush(user));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(Role role) {
        return userRepository.findAllByRoleAndActiveTrue(role).stream().map(userMapper::toResponse).toList();
    }

    private User saveWithUniqueEmailCheck(User user) {
        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new EmailAlreadyExistsException("Email is already registered.");
        }
    }

    private User findActiveUserById(Long id) {
        return userRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Active user not found."));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void validateBcryptPasswordLength(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_PASSWORD_BYTES) {
            throw new InvalidPasswordException("Password must be at most 72 UTF-8 bytes for BCrypt.");
        }
    }
}
