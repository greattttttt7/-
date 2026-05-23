package com.example.oslab.service.impl;

import com.example.oslab.dto.SubmissionRequest;
import com.example.oslab.exception.BusinessException;
import com.example.oslab.service.DockerJudgeService;
import com.example.oslab.service.JudgeExecutionStateService;
import com.example.oslab.vo.DockerJudgeResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class DockerJudgeServiceImpl implements DockerJudgeService {

    private static final long TIMEOUT_SECONDS = 15L;

    private final JudgeExecutionStateService judgeExecutionStateService;

    @Value("${oj.workspace-path:D:/Desktop/test}")
    private String workspacePath;

    @Value("${oj.docker-command:docker}")
    private String dockerCommand;

    @Value("${oj.docker-image:ucore-riscv:latest}")
    private String dockerImage;

    public DockerJudgeServiceImpl(JudgeExecutionStateService judgeExecutionStateService) {
        this.judgeExecutionStateService = judgeExecutionStateService;
    }

    @Override
    public DockerJudgeResult judge(SubmissionRequest request, Integer submissionId) {
        long startTime = System.currentTimeMillis();
        DockerJudgeResult result = new DockerJudgeResult();
        result.setSubmissionId(submissionId);

        if (request == null) {
            result.setSuccess(false);
            result.setMessage("评测请求不能为空");
            result.setCombinedLog(buildCombinedLog(null, result.getMessage()));
            result.setDurationMillis(System.currentTimeMillis() - startTime);
            return result;
        }

        Integer userId = request.getUserId();
        Integer labId = request.getLabId();

        Path hostFilePath = resolveHostFilePath(labId, submissionId, request.getFileName());
        String normalizedHostFilePath = hostFilePath.toAbsolutePath().toString().replace('\\', '/');
        String containerName = buildContainerName(submissionId);
        result.setSourceFilePath(normalizedHostFilePath);
        result.setHostFilePath(normalizedHostFilePath);
        result.setContainerName(containerName);

        Process process = null;
        CompletableFuture<String> stdoutFuture = null;
        CompletableFuture<String> stderrFuture = null;

        judgeExecutionStateService.markRunning(userId, labId);

        try {
            DockerJudgeRouteRegistry.JudgeRoute route = DockerJudgeRouteRegistry.resolve(labId, request.getFileName());
            result.setContainerPath(route.containerPath());
            result.setWorkDir(route.workDir());
            writeSourceFile(hostFilePath, request.getCodeContent());

            String normalizedFileName = normalizeFileName(request.getFileName());
            String bashCommand = buildJudgeBashCommand(route.workDir(), normalizedFileName);

            List<String> commandList = new ArrayList<>();
            commandList.add("docker");
            commandList.add("run");
            commandList.add("--rm");
            commandList.add("--name");
            commandList.add(containerName);
            
            // 1. 挂载学生提交的代码内核工作区
            commandList.add("-v");
            commandList.add(normalizedHostFilePath + ":" + route.containerPath());
            
            // 2. 【核心蜕变】不再零散挂载到只读系统目录！
            // 直接将你本地集齐了 6 个神装库的 libs 文件夹，整体打包挂载到容器内一个全新的自定义安全目录 /app/my_libs
            commandList.add("-v");
            commandList.add("D:/Desktop/test/libs:/app/my_libs");

            commandList.add(dockerImage);
            commandList.add("bash");
            commandList.add("-c");
            commandList.add(bashCommand);

            System.out.println("[DOCKER_RUN_COMMAND] " + String.join(" ", commandList));

            ProcessBuilder processBuilder = new ProcessBuilder(commandList);
            processBuilder.redirectErrorStream(false);

            process = processBuilder.start();
            stdoutFuture = readStreamAsync(process.getInputStream());
            stderrFuture = readStreamAsync(process.getErrorStream());

            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                result.setTimeout(true);
                result.setSuccess(false);
                result.setExitCode(-1);
                result.setMessage("运行超时");
                process.destroyForcibly();
                killContainerAsync(containerName);
            } else {
                int exitCode = process.exitValue();
                result.setExitCode(exitCode);
                result.setSuccess(exitCode == 0);
                result.setMessage(exitCode == 0 ? "运行完成" : "运行失败");
            }

            result.setStdout(joinQuietly(stdoutFuture));
            result.setStderr(joinQuietly(stderrFuture));
            result.setCombinedLog(buildCombinedLog(result.getStdout(), result.getStderr()));
        } catch (BusinessException e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
            result.setStderr(e.getMessage());
            result.setCombinedLog(buildCombinedLog(null, e.getMessage()));
        } catch (IOException e) {
            result.setSuccess(false);
            result.setMessage("评测执行失败：" + e.getMessage());
            result.setStderr(e.getMessage());
            result.setCombinedLog(buildCombinedLog(null, result.getMessage()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            result.setSuccess(false);
            result.setMessage("评测线程被中断");
            result.setStderr(e.getMessage());
            result.setCombinedLog(buildCombinedLog(null, e.getMessage()));
            killContainerAsync(containerName);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("评测失败：" + e.getMessage());
            result.setStderr(e.getMessage());
            result.setCombinedLog(buildCombinedLog(null, e.getMessage()));
        } finally {
            result.setDurationMillis(System.currentTimeMillis() - startTime);
            judgeExecutionStateService.clearRunning(userId, labId);
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }

        return result;
    }

    private Path resolveHostFilePath(Integer labId, Integer submissionId, String fileName) {
        String normalizedFileName = normalizeFileName(fileName);
        String labFolder = "lab" + labId;
        return Paths.get(workspacePath, "code_editor", labFolder, "sub_" + submissionId + "_" + normalizedFileName);
    }

    private String buildContainerName(Integer submissionId) {
        return "oslab_judge_" + submissionId;
    }

    private void writeSourceFile(Path hostFile, String codeContent) throws IOException {
        Path parent = hostFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
            Path tempFile = Files.createTempFile(parent, hostFile.getFileName().toString(), ".tmp");
            Files.writeString(tempFile, codeContent == null ? "" : codeContent, StandardCharsets.UTF_8);
            try {
                Files.move(tempFile, hostFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(tempFile, hostFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return;
        }
        Files.writeString(hostFile, codeContent == null ? "" : codeContent, StandardCharsets.UTF_8);
    }

    private CompletableFuture<String> readStreamAsync(InputStream inputStream) {
        return CompletableFuture.supplyAsync(() -> {
            StringBuilder builder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append(System.lineSeparator());
                }
            } catch (IOException e) {
                builder.append("[stream read error] ").append(e.getMessage());
            }
            return builder.toString();
        });
    }

    private String joinQuietly(CompletableFuture<String> future) {
        if (future == null) {
            return "";
        }
        try {
            return future.join();
        } catch (Exception e) {
            return "";
        }
    }

    private void killContainerAsync(String containerName) {
        CompletableFuture.runAsync(() -> {
            try {
                ProcessBuilder builder = new ProcessBuilder(dockerCommand, "kill", containerName);
                builder.redirectErrorStream(true);
                Process process = builder.start();
                process.waitFor(3, TimeUnit.SECONDS);
            } catch (Exception ignored) {
                // best effort cleanup
            }
        });
    }

    private String normalizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new BusinessException("任务文件名不能为空");
        }
        String normalized = fileName.trim().replace('\\', '/');
        return normalized.substring(normalized.lastIndexOf('/') + 1).toLowerCase(Locale.ROOT);
    }

    private String buildJudgeBashCommand(String workDir, String fileNameLower) {
        if (fileNameLower.endsWith(".py")) {
            return "cd " + workDir + " && python3 " + fileNameLower;
        }

        // 强行把我们自定义的 /app/my_libs 注入到最前面，给系统环境建立最高优先级的动态库寻址通道
        return "export LD_LIBRARY_PATH=/app/my_libs:$LD_LIBRARY_PATH && "
                + "cd " + workDir + " && "
                + "if grep -qE '^\\s*make\\s+-C\\s+user\\s+clean' Makefile; "
                + "then :; "
                + "else if grep -q '^clean:' Makefile; then make clean; fi; fi && "
                + "mkdir -p os && "
                + "printf '.align 4\\n.section .data\\n.global _app_num\\n_app_num:\\n.quad 0\\n' > os/link_app.S && "
                + "if [ -f \"os/kernel.ld\" ]; then cp os/kernel.ld os/kernel_app.ld; "
                + "else printf 'OUTPUT_ARCH(riscv)\\nENTRY(_start)\\nSECTIONS {\\n  . = 0x80200000;\\n  .text : { *(.text .text.*) }\\n  .data : { *(.data .data.*) }\\n  .bss : { *(.bss .bss.*) }\\n}' > os/kernel_app.ld; fi && "
                + "sed -i '/scripts\\/pack.py/d' Makefile && "
                + "sed -i '/python3.*pack.py/d' Makefile && "
                + "sed -i '/scripts\\/kernelld.py/d' Makefile && "
                + "sed -i '/python3.*kernelld.py/d' Makefile && "
                + "make run";
    }

    private String buildCombinedLog(String stdout, String stderr) {
        if ((stdout == null || stdout.isBlank()) && (stderr == null || stderr.isBlank())) {
            return "[OJ系统提示] 评测引擎正全速编译中，请稍后...";
        }

        StringBuilder builder = new StringBuilder();
        if (stdout != null && !stdout.isBlank()) {
            builder.append("[stdout]").append(System.lineSeparator()).append(stdout.trim()).append(System.lineSeparator());
        }
        if (stderr != null && !stderr.isBlank()) {
            builder.append("[stderr]").append(System.lineSeparator()).append(stderr.trim()).append(System.lineSeparator());
        }
        return builder.toString().trim();
    }
}