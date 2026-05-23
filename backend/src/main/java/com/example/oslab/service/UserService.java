package com.example.oslab.service;

import com.example.oslab.entity.User;

public interface UserService {
    User login(String username, String password);
    User register(String username, String password, String name);
    User getById(Integer userId);
}
