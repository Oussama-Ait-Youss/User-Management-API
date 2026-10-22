package com.usermanagementapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.usermanagementapi.dto.request.UserCreateRequest;
import com.usermanagementapi.dto.response.UserResponse;
import com.usermanagementapi.exception.GlobalExceptionHandler;
import com.usermanagementapi.exception.ResourceNotFoundException;
import com.usermanagementapi.model.Role;
import com.usermanagementapi.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

    private UserService service;
    private MockMvc mockMvc;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() {
        service = mock(UserService.class);
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(service), new ApiInfoController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
    }

    @Test
    void createsAdminWithoutAuthenticationOrPasswordInResponse() throws Exception {
        when(service.createUser(any(UserCreateRequest.class))).thenReturn(response(4L, Role.ADMIN));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"System Admin","email":"admin@example.com",
                                 "password":"Admin12345!","role":"ADMIN"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/users/4"))
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.name").value("System Admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void roleDefaultsToUserWhenOmitted() throws Exception {
        when(service.createUser(any(UserCreateRequest.class))).thenReturn(response(5L, Role.USER));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"User","email":"user@example.com","password":"password123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void rejectsInvalidCreateRequestWithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":" ","email":"not-an-email","password":"short","role":"USER"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/users"))
                .andExpect(jsonPath("$.fieldErrors.length()").value(3));
        verifyNoInteractions(service);
    }

    @Test
    void healthAndRootArePublic() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.application").value("User Management API"));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Running"));
        verifyNoInteractions(service);
    }

    @Test
    void getUserByIdReturnsNotFoundForMissingUser() throws Exception {
        when(service.getUserById(404L)).thenThrow(new ResourceNotFoundException("Active user not found."));

        mockMvc.perform(get("/api/users/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/api/users/404"));
    }

    @Test
    void passwordEndpointUsesUserIdAndReturnsSafeDto() throws Exception {
        when(service.changePassword(eq(4L), any())).thenReturn(response(4L, Role.ADMIN));

        mockMvc.perform(patch("/api/users/4/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"old-password","newPassword":"new-password"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    private UserResponse response(Long id, Role role) {
        return new UserResponse(id, "System Admin", "admin@example.com", role, true, null, null);
    }
}
