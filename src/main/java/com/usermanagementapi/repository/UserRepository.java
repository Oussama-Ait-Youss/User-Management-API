package com.usermanagementapi.repository;

import com.usermanagementapi.model.Role;
import com.usermanagementapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findAllByActiveTrue();

    Optional<User> findByIdAndActiveTrue(Long id);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    List<User> findAllByRoleAndActiveTrue(Role role);

    Optional<User> findByEmail(String email);
}
