package com.example.oslab.controller;

import com.example.oslab.common.Result;
import com.example.oslab.dto.SubmissionRequest;
import com.example.oslab.entity.Submission;
import com.example.oslab.service.DockerJudgeService;
import com.example.oslab.service.SubmissionService;
import com.example.oslab.vo.DockerJudgeResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;
    private final DockerJudgeService dockerJudgeService;

    public SubmissionController(SubmissionService submissionService, DockerJudgeService dockerJudgeService) {
        this.submissionService = submissionService;
        this.dockerJudgeService = dockerJudgeService;
    }

    @PostMapping("/snapshot")
    public Result<Submission> saveSnapshot(@Valid @RequestBody SubmissionRequest request) {
        return Result.success(submissionService.saveSnapshot(request));
    }

    @PostMapping("/judge")
    public Result<DockerJudgeResult> judge(@Valid @RequestBody SubmissionRequest request) {
        Submission submission = submissionService.createPending(request);
        DockerJudgeResult judgeResult = dockerJudgeService.judge(request, submission.getSubId());
        submissionService.updateJudgeResult(submission.getSubId(), request, judgeResult);
        return Result.success(judgeResult);
    }

    @PostMapping
    public Result<Submission> create(@Valid @RequestBody SubmissionRequest request) {
        return Result.success(submissionService.create(request));
    }

    @GetMapping
    public Result<List<Submission>> list(@RequestParam Integer userId, @RequestParam Integer labId) {
        return Result.success(submissionService.listByLabAndUser(userId, labId));
    }
}
