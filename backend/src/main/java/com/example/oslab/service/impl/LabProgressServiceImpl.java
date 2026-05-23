package com.example.oslab.service.impl;

import com.example.oslab.dto.ProgressRequest;
import com.example.oslab.entity.Lab;
import com.example.oslab.entity.LabProgress;
import com.example.oslab.entity.User;
import com.example.oslab.exception.BusinessException;
import com.example.oslab.mapper.LabMapper;
import com.example.oslab.mapper.LabProgressMapper;
import com.example.oslab.mapper.UserMapper;
import com.example.oslab.service.LabProgressService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LabProgressServiceImpl implements LabProgressService {

    private final LabProgressMapper labProgressMapper;
    private final LabMapper labMapper;
    private final UserMapper userMapper;

    public LabProgressServiceImpl(LabProgressMapper labProgressMapper, LabMapper labMapper, UserMapper userMapper) {
        this.labProgressMapper = labProgressMapper;
        this.labMapper = labMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<LabProgress> listByUserId(Integer userId) {
        ensureUserExists(userId);
        return labProgressMapper.selectByUserId(userId);
    }

    @Override
    public LabProgress getByUserIdAndLabId(Integer userId, Integer labId) {
        ensureUserExists(userId);
        ensureLabExists(labId);
        return labProgressMapper.selectByUserIdAndLabId(userId, labId);
    }

    @Override
    public LabProgress saveOrUpdate(ProgressRequest request) {
        return saveOrUpdate(request.getUserId(), request.getLabId(), request.getStatus(), request.getScore());
    }

    @Override
    public LabProgress saveOrUpdate(Integer userId, Integer labId, String status, Float score) {
        ensureUserExists(userId);
        ensureLabExists(labId);

        LabProgress progress = new LabProgress();
        progress.setUserId(userId);
        progress.setLabId(labId);
        progress.setStatus(status);
        progress.setScore(score);
        labProgressMapper.upsert(progress);
        return labProgressMapper.selectByUserIdAndLabId(userId, labId);
    }

    private void ensureUserExists(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
    }

    private void ensureLabExists(Integer labId) {
        Lab lab = labMapper.selectById(labId);
        if (lab == null) {
            throw new BusinessException("实验不存在");
        }
    }
}
