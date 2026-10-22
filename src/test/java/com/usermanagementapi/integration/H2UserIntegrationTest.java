package com.usermanagementapi.integration;

import com.usermanagementapi.model.Role;
import com.usermanagementapi.model.User;
import com.usermanagementapi.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(com.usermanagementapi.config.RootConfig.class)
class H2UserIntegrationTest {

    private static final String DATABASE_NAME = "user_management_test_" + UUID.randomUUID();

    @Autowired
    private UserRepository repository;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("db.url", () -> "jdbc:h2:mem:" + DATABASE_NAME + ";DB_CLOSE_DELAY=-1");
        registry.add("hibernate.hbm2ddl.auto", () -> "create-drop");
    }

    @Test
    void persistsUserWithUniqueNormalizedEmailAndTimestamps() {
        User user = new User("Integration User", "TEST@EXAMPLE.COM", "bcrypt-hash", Role.ADMIN);
        User saved = repository.saveAndFlush(user);

        User found = repository.findByIdAndActiveTrue(saved.getId()).orElseThrow();

        assertEquals("test@example.com", found.getEmail());
        assertEquals(Role.ADMIN, found.getRole());
        assertTrue(found.getActive());
        assertNotNull(found.getCreatedAt());
        assertNotNull(found.getUpdatedAt());
    }
}
