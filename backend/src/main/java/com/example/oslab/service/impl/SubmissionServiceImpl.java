package com.example.oslab.service.impl;

import com.example.oslab.dto.SubmissionRequest;
import com.example.oslab.entity.Lab;
import com.example.oslab.entity.Submission;
import com.example.oslab.entity.Task;
import com.example.oslab.entity.User;
import com.example.oslab.exception.BusinessException;
import com.example.oslab.mapper.LabMapper;
import com.example.oslab.mapper.SubmissionMapper;
import com.example.oslab.mapper.TaskMapper;
import com.example.oslab.mapper.UserMapper;
import com.example.oslab.service.LabProgressService;
import com.example.oslab.service.SubmissionService;
import com.example.oslab.service.UserCodeService;
import com.example.oslab.vo.DockerJudgeResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionMapper submissionMapper;
    private final LabMapper labMapper;
    private final TaskMapper taskMapper;
    private final UserMapper userMapper;
    private final LabProgressService labProgressService;
    private final UserCodeService userCodeService;

    public SubmissionServiceImpl(
            SubmissionMapper submissionMapper,
            LabMapper labMapper,
            TaskMapper taskMapper,
            UserMapper userMapper,
            LabProgressService labProgressService,
            UserCodeService userCodeService
    ) {
        this.submissionMapper = submissionMapper;
        this.labMapper = labMapper;
        this.taskMapper = taskMapper;
        this.userMapper = userMapper;
        this.labProgressService = labProgressService;
        this.userCodeService = userCodeService;
    }

    @Override
    public String loadTaskSource(Integer userId, Integer taskId) {
        ensureUserExists(userId);
        Task task = ensureTaskExists(taskId);
        return userCodeService.loadUserCode(userId, task.getLabId(), extractFileName(task.getFilePath()));
    }

    @Override
    public String loadTaskAnswer(Integer taskId) {
        return readFileContent(ensureTaskExists(taskId).getAnswerPath());
    }

    @Override
    public String loadAnswerFile(Integer labId, String fileName) {
        if (labId == null || fileName == null || fileName.isBlank()) {
            throw new BusinessException("参数错误：labId 和 fileName 不能为空");
        }
        String normalizedName = fileName.trim().replace('\\', '/');
        normalizedName = normalizedName.substring(normalizedName.lastIndexOf('/') + 1);
        Path answerPath = Paths.get("D:/Desktop/test/answer/answer",
                "lab" + labId, normalizedName);
        return readFileContent(answerPath.toString());
    }

    @Override
    public Task getTask(Integer taskId) {
        return ensureTaskExists(taskId);
    }

    @Override
    public Task getFirstTaskByLabId(Integer labId) {
        ensureLabExists(labId);
        List<Task> tasks = taskMapper.selectByLabId(labId);
        if (tasks == null || tasks.isEmpty()) {
            throw new BusinessException("实验暂无任务");
        }
        return tasks.get(0);
    }

    @Override
    public Submission create(SubmissionRequest request) {
        ensureUserExists(request.getUserId());
        ensureLabExists(request.getLabId());
        ensureTaskExists(request.getTaskId());

        String fileName = extractFileName(request.getFileName());
        userCodeService.saveUserCode(request.getUserId(), request.getLabId(), fileName, request.getCodeContent());

        String filePath = userCodeService.getUserCodeFilePath(request.getUserId(), request.getLabId(), fileName);
        Submission submission = buildSubmission(request, filePath, "Fail", 0f, 0, 0, 0, "");
        submissionMapper.insert(submission);
        return submission;
    }

    @Override
    public Submission saveSnapshot(SubmissionRequest request) {
        ensureUserExists(request.getUserId());
        ensureLabExists(request.getLabId());
        ensureTaskExists(request.getTaskId());

        String fileName = extractFileName(request.getFileName());
        userCodeService.saveUserCode(request.getUserId(), request.getLabId(), fileName, request.getCodeContent());

        String filePath = userCodeService.getUserCodeFilePath(request.getUserId(), request.getLabId(), fileName);
        Submission submission = buildSubmission(request, filePath, "Fail", 0f, 0, 0, 0, "");
        submissionMapper.insert(submission);
        return submission;
    }

    @Override
    public Submission createPending(SubmissionRequest request) {
        ensureUserExists(request.getUserId());
        ensureLabExists(request.getLabId());
        ensureTaskExists(request.getTaskId());
        Submission submission = buildSubmission(request, "", "Fail", 0f, 0, 0, 0, "");
        submissionMapper.insert(submission);
        return submission;
    }

    @Override
    public Submission updateJudgeResult(Integer submissionId, SubmissionRequest request, DockerJudgeResult judgeResult) {
        ensureUserExists(request.getUserId());
        ensureLabExists(request.getLabId());
        ensureTaskExists(request.getTaskId());

        String fileName = extractFileName(request.getFileName());
        userCodeService.saveUserCode(request.getUserId(), request.getLabId(), fileName, request.getCodeContent());

        String filePath = userCodeService.getUserCodeFilePath(request.getUserId(), request.getLabId(), fileName);

        boolean success = judgeResult != null && judgeResult.isSuccess();
        String runResult = success ? "Pass" : "Fail";
        float runTime = judgeResult == null ? 0f : (float) (judgeResult.getDurationMillis() / 1000.0);
        int passCount = success ? 1 : 0;
        int failCount = success ? 0 : 1;

        Submission submission = new Submission();
        submission.setSubId(submissionId);
        submission.setUserId(request.getUserId());
        submission.setLabId(request.getLabId());
        submission.setCodeContent(filePath.replace('\\', '/'));
        submission.setRunResult(runResult);
        submission.setRunTime(runTime);
        submission.setPassCount(passCount);
        submission.setFailCount(failCount);
        submission.setTotalCount(1);
        submission.setResultDetail(judgeResult == null || judgeResult.getCombinedLog() == null ? "" : judgeResult.getCombinedLog());
        submission.setCommitHash(null);
        submissionMapper.updateResult(submission);

        if (success) {
            labProgressService.saveOrUpdate(request.getUserId(), request.getLabId(), "已完成", 100f);
        } else {
            labProgressService.saveOrUpdate(request.getUserId(), request.getLabId(), "进行中", 0f);
        }

        return submission;
    }

    @Override
    public Submission createWithJudge(SubmissionRequest request, DockerJudgeResult judgeResult) {
        Submission submission = createPending(request);
        return updateJudgeResult(submission.getSubId(), request, judgeResult);
    }

    @Override
    public List<Submission> listByLabAndUser(Integer userId, Integer labId) {
        ensureUserExists(userId);
        ensureLabExists(labId);
        return submissionMapper.selectByUserIdAndLabId(userId, labId);
    }

    private Submission buildSubmission(SubmissionRequest request,
                                       String codePath,
                                       String runResult,
                                       float runTime,
                                       int passCount,
                                       int failCount,
                                       int totalCount,
                                       String resultDetail) {
        Submission submission = new Submission();
        submission.setUserId(request.getUserId());
        submission.setLabId(request.getLabId());
        submission.setCodeContent(codePath);
        submission.setRunResult(runResult);
        submission.setRunTime(runTime);
        submission.setPassCount(passCount);
        submission.setFailCount(failCount);
        submission.setTotalCount(totalCount);
        submission.setResultDetail(resultDetail);
        submission.setSubmitTime(LocalDateTime.now());
        return submission;
    }

    private String extractFileName(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new BusinessException("文件路径不能为空");
        }
        String normalized = filePath.trim().replace('\\', '/');
        return normalized.substring(normalized.lastIndexOf('/') + 1);
    }

    private Task ensureTaskExists(Integer taskId) {
        if (taskId == null) {
            throw new BusinessException("任务不存在");
        }
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        return task;
    }

    private void ensureUserExists(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
    }

    private void ensureLabExists(Integer labId) {
        Lab lab = labMapper.selectById(labId);
        if (lab == null) {
            throw new BusinessException("实验不存在");
        }
    }

    private String readFileContent(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                return "";
            }
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new BusinessException("读取文件失败：" + e.getMessage());
        }
    }
}
