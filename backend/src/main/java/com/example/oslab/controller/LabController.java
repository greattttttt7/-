package com.example.oslab.controller;

import com.example.oslab.common.Result;
import com.example.oslab.service.LabService;
import com.example.oslab.vo.LabDetailVO;
import com.example.oslab.entity.Lab;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/labs")
public class LabController {

    private final LabService labService;

    public LabController(LabService labService) {
        this.labService = labService;
    }

    @GetMapping
    public Result<List<Lab>> list() {
        return Result.success(labService.listLabs());
    }

    @GetMapping("/{labId}")
    public Result<LabDetailVO> detail(@PathVariable Integer labId) {
        return Result.success(labService.getLabDetail(labId));
    }
}
