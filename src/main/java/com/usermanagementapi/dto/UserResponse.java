package com.usermanagementapi.dto;

import com.usermanagementapi.model.enums.UserRole;
import org.hibernate.property.access.internal.PropertyAccessStrategyNoopImpl;

import java.time.LocalDate;

public class UserResponse {



    private Long id;
    private String first_name;
    private String last_name;
    private UserRole role;
    private String email;
    private String password;
    private boolean isActive;


    public UserResponse(String first_name,String last_name,UserRole role,String email,String password,boolean isActive){
        this.first_name = first_name;
        this.last_name = last_name;
        this.role = role;
        this.email = email;
        this.email = email;
        this.password = password;
        this.isActive = isActive;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return first_name;
    }

    public void setFirstName(String first_name) {
        this.first_name = first_name;
    }

    public String getLastName() {
        return last_name;
    }

    public void setLastName(String last_name) {
        this.last_name = last_name;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
