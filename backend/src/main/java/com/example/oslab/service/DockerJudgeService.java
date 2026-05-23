package com.example.oslab.service;

import com.example.oslab.dto.SubmissionRequest;
import com.example.oslab.vo.DockerJudgeResult;

public interface DockerJudgeService {
    DockerJudgeResult judge(SubmissionRequest request, Integer submissionId);
}
