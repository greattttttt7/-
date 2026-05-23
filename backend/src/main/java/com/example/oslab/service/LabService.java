package com.example.oslab.service;

import com.example.oslab.entity.Lab;
import com.example.oslab.vo.LabDetailVO;

import java.util.List;

public interface LabService {
    List<Lab> listLabs();
    LabDetailVO getLabDetail(Integer labId);
}
