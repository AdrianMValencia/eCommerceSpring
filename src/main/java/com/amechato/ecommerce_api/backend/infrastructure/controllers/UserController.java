package com.amechato.ecommerce_api.backend.infrastructure.controllers;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.amechato.ecommerce_api.backend.application.usecases.UserService;
import com.amechato.ecommerce_api.backend.domain.models.User;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @PostMapping()
    public User save(@RequestBody User user) {
        return this.userService.save(user);
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable Integer id) {
        return this.userService.findById(id);
    }

    @GetMapping("/by-email")
    public User getByEmail(@RequestParam String email) {
        return this.userService.findByEmail(email);
    }
}
