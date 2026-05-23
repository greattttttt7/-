package com.example.oslab.controller;

import com.example.oslab.common.Result;
import com.example.oslab.entity.Document;
import com.example.oslab.mapper.DocumentMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentMapper documentMapper;

    public DocumentController(DocumentMapper documentMapper) {
        this.documentMapper = documentMapper;
    }

    @GetMapping("/lab/{labId}")
    public Result<Document> byLabId(@PathVariable Integer labId) {
        return Result.success(documentMapper.selectByLabId(labId));
    }
}
