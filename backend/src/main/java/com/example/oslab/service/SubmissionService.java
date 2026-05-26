package com.example.oslab.service;

import com.example.oslab.dto.SubmissionRequest;
import com.example.oslab.entity.Submission;
import com.example.oslab.entity.Task;
import com.example.oslab.vo.DockerJudgeResult;

import java.util.List;

public interface SubmissionService {
    String loadTaskSource(Integer userId, Integer taskId);

    String loadTaskAnswer(Integer taskId);

    String loadAnswerFile(Integer labId, String fileName);

    Task getFirstTaskByLabId(Integer labId);

    Task getTask(Integer taskId);

    Submission create(SubmissionRequest request);

    Submission saveSnapshot(SubmissionRequest request);

    Submission createPending(SubmissionRequest request);

    Submission updateJudgeResult(Integer submissionId, SubmissionRequest request, DockerJudgeResult judgeResult);

    Submission createWithJudge(SubmissionRequest request, DockerJudgeResult judgeResult);

    List<Submission> listByLabAndUser(Integer userId, Integer labId);
}
