package com.example.oslab.mapper;

import com.example.oslab.entity.LabProgress;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LabProgressMapper {
    List<LabProgress> selectByUserId(@Param("userId") Integer userId);
    LabProgress selectByUserIdAndLabId(@Param("userId") Integer userId, @Param("labId") Integer labId);
    int upsert(LabProgress progress);
}
