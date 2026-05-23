package com.example.oslab.service.impl;

import com.example.oslab.exception.BusinessException;
import com.example.oslab.service.UserCodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class UserCodeServiceImpl implements UserCodeService {

    private static final Logger logger = LoggerFactory.getLogger(UserCodeServiceImpl.class);

    @Value("${code-editor.base-path:D:/Desktop/test/code_editor}")
    private String codeEditorBasePath;

    @Value("${code-editor.template-path:D:/Desktop/test/hollow/hollow}")
    private String templatePath;

    @Value("${code-editor.user-folder-prefix:user_}")
    private String userFolderPrefix;

    private final Object fileLock = new Object();

    @Override
    public void initializeUserCodeDirectory(Integer userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        Path userDir = getUserDirectory(userId);

        try {
            Files.createDirectories(userDir);
            logger.info("用户代码目录已就绪: {}", userDir);
        } catch (IOException e) {
            logger.error("初始化用户代码目录失败: userId={}, error={}", userId, e.getMessage(), e);
            throw new BusinessException("初始化用户代码目录失败: " + e.getMessage());
        }
    }

    @Override
    public String loadUserCode(Integer userId, Integer labId, String fileName) {
        if (userId == null || labId == null || fileName == null) {
            throw new BusinessException("参数不能为空");
        }

        Path userCodeFile = Paths.get(getUserCodeFilePath(userId, labId, fileName));
        Path templateFile = Paths.get(getTemplateFilePath(labId, fileName));

        try {
            if (Files.exists(userCodeFile)) {
                String content = Files.readString(userCodeFile, StandardCharsets.UTF_8);
                logger.debug("从用户目录加载代码: userId={}, labId={}, file={}", userId, labId, fileName);
                return content;
            }

            if (Files.exists(templateFile)) {
                String content = Files.readString(templateFile, StandardCharsets.UTF_8);
                logger.debug("从模板目录加载代码: userId={}, labId={}, file={}", userId, labId, fileName);
                return content;
            }

            logger.warn("模板文件不存在: labId={}, file={}", labId, fileName);
            return "";
        } catch (IOException e) {
            logger.error("读取代码文件失败: userId={}, labId={}, file={}, error={}",
                userId, labId, fileName, e.getMessage(), e);
            throw new BusinessException("读取代码文件失败: " + e.getMessage());
        }
    }

    @Override
    public void saveUserCode(Integer userId, Integer labId, String fileName, String codeContent) {
        if (userId == null || labId == null || fileName == null) {
            throw new BusinessException("参数不能为空");
        }

        Path userCodeFile = Paths.get(getUserCodeFilePath(userId, labId, fileName));

        synchronized (fileLock) {
            try {
                Path parentDir = userCodeFile.getParent();
                if (parentDir != null && !Files.exists(parentDir)) {
                    Files.createDirectories(parentDir);
                }

                Path tempFile = Files.createTempFile(parentDir,
                    userCodeFile.getFileName().toString() + "_", ".tmp");

                Files.writeString(tempFile, codeContent != null ? codeContent : "", StandardCharsets.UTF_8);

                try {
                    Files.move(tempFile, userCodeFile,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(tempFile, userCodeFile, StandardCopyOption.REPLACE_EXISTING);
                }

                logger.debug("保存用户代码: userId={}, labId={}, file={}", userId, labId, fileName);

            } catch (IOException e) {
                logger.error("保存代码文件失败: userId={}, labId={}, file={}, error={}",
                    userId, labId, fileName, e.getMessage(), e);
                throw new BusinessException("保存代码文件失败: " + e.getMessage());
            }
        }
    }

    @Override
    public String getUserCodeFilePath(Integer userId, Integer labId, String fileName) {
        Path userDir = getUserDirectory(userId);
        Path labDir = userDir.resolve("lab" + labId);
        return labDir.resolve(fileName).toString();
    }

    @Override
    public String getTemplateFilePath(Integer labId, String fileName) {
        Path templateDir = Paths.get(templatePath);
        Path labDir = templateDir.resolve("lab" + labId);
        return labDir.resolve(fileName).toString();
    }

    @Override
    public boolean userCodeExists(Integer userId, Integer labId, String fileName) {
        Path userCodeFile = Paths.get(getUserCodeFilePath(userId, labId, fileName));
        return Files.exists(userCodeFile);
    }

    private Path getUserDirectory(Integer userId) {
        return Paths.get(codeEditorBasePath, userFolderPrefix + userId);
    }
}