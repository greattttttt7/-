package com.example.oslab.service;

import com.example.oslab.dto.ProgressRequest;
import com.example.oslab.entity.LabProgress;

import java.util.List;

public interface LabProgressService {
    List<LabProgress> listByUserId(Integer userId);
    LabProgress getByUserIdAndLabId(Integer userId, Integer labId);
    LabProgress saveOrUpdate(ProgressRequest request);
    LabProgress saveOrUpdate(Integer userId, Integer labId, String status, Float score);
}
