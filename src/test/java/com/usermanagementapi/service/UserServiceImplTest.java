package com.usermanagementapi.service;

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
import com.usermanagementapi.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private UserRepository repository;
    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(UserRepository.class);
        service = new UserServiceImpl(repository, new UserMapper());
    }

    @Test
    void createsUserWithBcryptPasswordAndDefaultRole() {
        UserCreateRequest request = new UserCreateRequest("Ada Lovelace", " ADA@EXAMPLE.COM ",
                "password123", null);
        when(repository.existsByEmail("ada@example.com")).thenReturn(false);
        when(repository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        UserResponse response = service.createUser(request);
        User saved = captureSavedUser();

        assertEquals(Role.USER, response.role());
        assertEquals("ada@example.com", response.email());
        assertEquals("Ada Lovelace", response.name());
        assertEquals(Role.USER, saved.getRole());
        assertTrue(BCrypt.checkpw("password123", saved.getPassword()));
        assertNotEquals("password123", saved.getPassword());
        assertNotNull(response);
    }

    @Test
    void permitsCreatingAdmin() {
        UserCreateRequest request = new UserCreateRequest("System Admin", "admin@example.com",
                "Admin12345!", Role.ADMIN);
        when(repository.existsByEmail("admin@example.com")).thenReturn(false);
        when(repository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(Role.ADMIN, service.createUser(request).role());
    }

    @Test
    void rejectsExistingEmail() {
        when(repository.existsByEmail("user@example.com")).thenReturn(true);
        UserCreateRequest request = new UserCreateRequest("User", "user@example.com", "password123", Role.USER);

        assertThrows(EmailAlreadyExistsException.class, () -> service.createUser(request));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void catchesEmailRaceAtDatabaseConstraint() {
        when(repository.existsByEmail("user@example.com")).thenReturn(false);
        when(repository.saveAndFlush(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate"));
        UserCreateRequest request = new UserCreateRequest("User", "user@example.com", "password123", Role.USER);

        assertThrows(EmailAlreadyExistsException.class, () -> service.createUser(request));
    }

    @Test
    void listsOnlyActiveUsers() {
        User active = user(1L, "active@example.com", Role.USER, true);
        when(repository.findAllByActiveTrue()).thenReturn(List.of(active));

        assertEquals(1, service.getAllUsers().size());
        verify(repository).findAllByActiveTrue();
    }

    @Test
    void findsUserByIdAndHidesInactiveUsers() {
        User active = user(7L, "user@example.com", Role.USER, true);
        when(repository.findByIdAndActiveTrue(7L)).thenReturn(Optional.of(active));

        assertEquals(7L, service.getUserById(7L).id());
        when(repository.findByIdAndActiveTrue(8L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getUserById(8L));
    }

    @Test
    void updatesNameAndEmailButDoesNotChangeRole() {
        User target = user(7L, "old@example.com", Role.ADMIN, true);
        when(repository.findByIdAndActiveTrue(7L)).thenReturn(Optional.of(target));
        when(repository.existsByEmailAndIdNot("new@example.com", 7L)).thenReturn(false);
        when(repository.saveAndFlush(target)).thenReturn(target);

        UserResponse updated = service.updateUser(7L, new UserUpdateRequest("Updated Name", "new@example.com"));

        assertEquals("Updated Name", updated.name());
        assertEquals("new@example.com", updated.email());
        assertEquals(Role.ADMIN, updated.role());
    }

    @Test
    void rejectsDuplicateEmailDuringUpdate() {
        User target = user(7L, "old@example.com", Role.USER, true);
        when(repository.findByIdAndActiveTrue(7L)).thenReturn(Optional.of(target));
        when(repository.existsByEmailAndIdNot("taken@example.com", 7L)).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () ->
                service.updateUser(7L, new UserUpdateRequest("User", "taken@example.com")));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void deactivatesUserWithoutDeleting() {
        User target = user(7L, "user@example.com", Role.USER, true);
        when(repository.findByIdAndActiveTrue(7L)).thenReturn(Optional.of(target));

        service.deactivateUser(7L);

        assertFalse(target.getActive());
        verify(repository, never()).delete(any());
    }

    @Test
    void filtersActiveUsersByRole() {
        when(repository.findAllByRoleAndActiveTrue(Role.ADMIN))
                .thenReturn(List.of(user(1L, "admin@example.com", Role.ADMIN, true)));

        assertEquals(Role.ADMIN, service.getUsersByRole(Role.ADMIN).get(0).role());
        verify(repository).findAllByRoleAndActiveTrue(Role.ADMIN);
    }

    @Test
    void changesPasswordWhenCurrentPasswordMatches() {
        User target = user(7L, "user@example.com", Role.USER, true);
        target.setPassword(BCrypt.hashpw("old-password", BCrypt.gensalt()));
        when(repository.findByIdAndActiveTrue(7L)).thenReturn(Optional.of(target));
        when(repository.saveAndFlush(target)).thenReturn(target);
        ChangePasswordRequest request = passwordRequest("old-password", "new-password");

        UserResponse response = service.changePassword(7L, request);

        assertTrue(BCrypt.checkpw("new-password", target.getPassword()));
        assertNotNull(response);
    }

    @Test
    void rejectsIncorrectCurrentPasswordWithoutUpdating() {
        User target = user(7L, "user@example.com", Role.USER, true);
        String previousHash = BCrypt.hashpw("old-password", BCrypt.gensalt());
        target.setPassword(previousHash);
        when(repository.findByIdAndActiveTrue(7L)).thenReturn(Optional.of(target));

        assertThrows(InvalidPasswordException.class,
                () -> service.changePassword(7L, passwordRequest("wrong", "new-password")));
        assertEquals(previousHash, target.getPassword());
        verify(repository, never()).saveAndFlush(target);
    }

    @Test
    void rejectsBcryptPasswordsOver72Utf8Bytes() {
        UserCreateRequest request = new UserCreateRequest("User", "user@example.com", "é".repeat(37), Role.USER);

        assertThrows(InvalidPasswordException.class, () -> service.createUser(request));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsOverlongCurrentPasswordBeforeBcryptComparison() {
        User target = user(7L, "user@example.com", Role.USER, true);
        target.setPassword(BCrypt.hashpw("old-password", BCrypt.gensalt()));
        when(repository.findByIdAndActiveTrue(7L)).thenReturn(Optional.of(target));

        assertThrows(InvalidPasswordException.class, () ->
                service.changePassword(7L, passwordRequest("é".repeat(37), "new-password")));
        verify(repository, never()).saveAndFlush(target);
    }

    private User captureSavedUser() {
        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(repository).saveAndFlush(captor.capture());
        return captor.getValue();
    }

    private User user(Long id, String email, Role role, boolean active) {
        User user = new User("Test User", email, "hash", role);
        user.setId(id);
        user.setActive(active);
        return user;
    }

    private ChangePasswordRequest passwordRequest(String current, String next) {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword(current);
        request.setNewPassword(next);
        return request;
    }
}
