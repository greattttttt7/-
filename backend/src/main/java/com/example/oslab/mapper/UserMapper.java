package com.example.oslab.mapper;

import com.example.oslab.entity.User;

public interface UserMapper {
    User selectByUsername(String username);
    User selectById(Integer userId);
    int insert(User user);
}
