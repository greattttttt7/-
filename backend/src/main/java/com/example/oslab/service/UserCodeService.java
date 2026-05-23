package com.example.oslab.service;

public interface UserCodeService {
    void initializeUserCodeDirectory(Integer userId);
    String loadUserCode(Integer userId, Integer labId, String fileName);
    void saveUserCode(Integer userId, Integer labId, String fileName, String codeContent);
    String getUserCodeFilePath(Integer userId, Integer labId, String fileName);
    String getTemplateFilePath(Integer labId, String fileName);
    boolean userCodeExists(Integer userId, Integer labId, String fileName);
}