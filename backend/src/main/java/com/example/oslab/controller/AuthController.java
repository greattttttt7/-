package com.example.oslab.controller;

import com.example.oslab.common.Result;
import com.example.oslab.dto.LoginRequest;
import com.example.oslab.dto.RegisterRequest;
import com.example.oslab.entity.User;
import com.example.oslab.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/auth", "/auth"})
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping({"/login", "/signin"})
    public Result<User> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request.getUsername(), request.getPassword()));
    }

    @PostMapping({"/register", "/signup"})
    public Result<User> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(userService.register(request.getUsername(), request.getPassword(), request.getName()));
    }
}
