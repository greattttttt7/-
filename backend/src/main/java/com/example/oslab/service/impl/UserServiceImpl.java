package com.example.oslab.service.impl;

import com.example.oslab.entity.Lab;
import com.example.oslab.entity.LabProgress;
import com.example.oslab.entity.User;
import com.example.oslab.exception.BusinessException;
import com.example.oslab.mapper.LabMapper;
import com.example.oslab.mapper.LabProgressMapper;
import com.example.oslab.mapper.UserMapper;
import com.example.oslab.service.UserCodeService;
import com.example.oslab.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final LabMapper labMapper;
    private final LabProgressMapper labProgressMapper;
    private final UserCodeService userCodeService;

    public UserServiceImpl(UserMapper userMapper, LabMapper labMapper, LabProgressMapper labProgressMapper, UserCodeService userCodeService) {
        this.userMapper = userMapper;
        this.labMapper = labMapper;
        this.labProgressMapper = labProgressMapper;
        this.userCodeService = userCodeService;
    }

    @Override
    public User login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        String passwordMd5 = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        String storedPassword = user.getPassword();
        if (!passwordMd5.equals(storedPassword) && !password.equals(storedPassword)) {
            throw new BusinessException("密码错误");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    public User register(String username, String password, String name) {
        if (userMapper.selectByUsername(username) != null) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setName(name);
        user.setPassword(DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8)));
        userMapper.insert(user);
        
        initDefaultProgress(user.getUserId());
        userCodeService.initializeUserCodeDirectory(user.getUserId());
        
        user.setPassword(null);
        return user;
    }

    @Override
    public User getById(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    private void initDefaultProgress(Integer userId) {
        List<Lab> labs = labMapper.selectAll();
        for (Lab lab : labs) {
            LabProgress progress = new LabProgress();
            progress.setUserId(userId);
            progress.setLabId(lab.getLabId());
            progress.setStatus("未开始");
            progress.setScore(0f);
            labProgressMapper.upsert(progress);
        }
    }
}
