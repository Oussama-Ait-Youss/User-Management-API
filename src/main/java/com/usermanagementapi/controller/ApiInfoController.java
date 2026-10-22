package com.usermanagementapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ApiInfoController {

    @GetMapping("/")
    public Map<String, String> root() {
        return Map.of("application", "User Management API", "status", "Running");
    }

    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "application", "User Management API");
    }
}
