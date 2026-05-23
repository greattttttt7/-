package com.example.oslab.controller;

import com.example.oslab.service.UserCodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user-code")
public class UserCodeController {

    private final UserCodeService userCodeService;

    public UserCodeController(UserCodeService userCodeService) {
        this.userCodeService = userCodeService;
    }

    @GetMapping("/load")
    public ResponseEntity<Map<String, Object>> loadCode(
            @RequestParam Integer userId,
            @RequestParam Integer labId,
            @RequestParam String fileName) {
        
        String codeContent = userCodeService.loadUserCode(userId, labId, fileName);
        String filePath = userCodeService.getUserCodeFilePath(userId, labId, fileName);
        boolean exists = userCodeService.userCodeExists(userId, labId, fileName);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("codeContent", codeContent);
        response.put("filePath", filePath);
        response.put("exists", exists);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveCode(
            @RequestParam Integer userId,
            @RequestParam Integer labId,
            @RequestParam String fileName,
            @RequestBody String codeContent) {
        
        userCodeService.saveUserCode(userId, labId, fileName, codeContent);
        String filePath = userCodeService.getUserCodeFilePath(userId, labId, fileName);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "代码保存成功");
        response.put("filePath", filePath);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/path")
    public ResponseEntity<Map<String, Object>> getFilePath(
            @RequestParam Integer userId,
            @RequestParam Integer labId,
            @RequestParam String fileName) {
        
        String userFilePath = userCodeService.getUserCodeFilePath(userId, labId, fileName);
        String templateFilePath = userCodeService.getTemplateFilePath(labId, fileName);
        boolean exists = userCodeService.userCodeExists(userId, labId, fileName);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("userFilePath", userFilePath);
        response.put("templateFilePath", templateFilePath);
        response.put("exists", exists);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/initialize/{userId}")
    public ResponseEntity<Map<String, Object>> initializeUserDirectory(@PathVariable Integer userId) {
        userCodeService.initializeUserCodeDirectory(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "用户代码目录初始化成功");
        
        return ResponseEntity.ok(response);
    }
}