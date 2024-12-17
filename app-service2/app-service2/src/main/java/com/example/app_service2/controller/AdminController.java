package com.example.app_service2.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app-service2")
public class AdminController {

    @GetMapping("/admin-endpoint")
    public String adminEndpoint() {
        return "This is a protected admin endpoint.";
    }
}

