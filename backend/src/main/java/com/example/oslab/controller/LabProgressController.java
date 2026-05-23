package com.example.oslab.controller;

import com.example.oslab.common.Result;
import com.example.oslab.dto.ProgressRequest;
import com.example.oslab.entity.LabProgress;
import com.example.oslab.service.LabProgressService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
public class LabProgressController {

    private final LabProgressService labProgressService;

    public LabProgressController(LabProgressService labProgressService) {
        this.labProgressService = labProgressService;
    }

    @GetMapping("/{userId}")
    public Result<List<LabProgress>> list(@PathVariable Integer userId) {
        return Result.success(labProgressService.listByUserId(userId));
    }

    @GetMapping("/one")
    public Result<LabProgress> one(@RequestParam Integer userId, @RequestParam Integer labId) {
        return Result.success(labProgressService.getByUserIdAndLabId(userId, labId));
    }

    @PostMapping
    public Result<LabProgress> saveOrUpdate(@Valid @RequestBody ProgressRequest request) {
        return Result.success(labProgressService.saveOrUpdate(request));
    }
}
