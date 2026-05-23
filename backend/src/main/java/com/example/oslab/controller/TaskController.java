package com.example.oslab.controller;

import com.example.oslab.common.Result;
import com.example.oslab.entity.Task;
import com.example.oslab.service.SubmissionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final SubmissionService submissionService;

    public TaskController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @GetMapping("/{taskId}")
    public Result<Task> detail(@PathVariable Integer taskId) {
        return Result.success(submissionService.getTask(taskId));
    }

    @GetMapping("/{taskId}/source")
    public Result<String> source(@PathVariable Integer taskId, @RequestParam Integer userId) {
        return Result.success(submissionService.loadTaskSource(userId, taskId));
    }

    @GetMapping("/{taskId}/answer")
    public Result<String> answer(@PathVariable Integer taskId) {
        return Result.success(submissionService.loadTaskAnswer(taskId));
    }
}
