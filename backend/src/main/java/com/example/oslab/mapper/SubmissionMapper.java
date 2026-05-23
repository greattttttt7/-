package com.example.oslab.mapper;

import com.example.oslab.entity.Submission;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SubmissionMapper {
    int insert(Submission submission);
    int updateResult(Submission submission);
    List<Submission> selectByUserIdAndLabId(@Param("userId") Integer userId, @Param("labId") Integer labId);
}
