package com.usermanagementapi.dto;

import com.usermanagementapi.model.enums.UserRole;
import org.springframework.data.convert.Jsr310Converters;

public class UserCreatRequest {
    private String first_name;
    private String last_name;
    private UserRole role;
    private String email;
    private String password;




    public UserCreatRequest(){

    }


    public String getFirst_name() {
        return first_name;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
