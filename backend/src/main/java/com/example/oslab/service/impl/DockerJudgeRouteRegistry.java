package com.example.oslab.service.impl;

import com.example.oslab.exception.BusinessException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

final class DockerJudgeRouteRegistry {

    private static final String CONTAINER_BASE_PATH = "/home/ucore";
    private static final Map<String, Map<String, JudgeRoute>> ROUTES = buildRoutes();

    private DockerJudgeRouteRegistry() {
    }

    static JudgeRoute resolve(Integer labId, String fileName) {
        if (labId == null) {
            throw new BusinessException("实验不存在");
        }
        String normalizedFileName = normalizeFileName(fileName);
        Map<String, JudgeRoute> labRoutes = ROUTES.get(labKey(labId));
        if (labRoutes == null || !labRoutes.containsKey(normalizedFileName)) {
            throw new BusinessException("未找到评测路由：" + labKey(labId) + "/" + normalizedFileName);
        }
        return labRoutes.get(normalizedFileName);
    }

    private static Map<String, Map<String, JudgeRoute>> buildRoutes() {
        Map<String, Map<String, JudgeRoute>> routes = new HashMap<>();

        // Lab1
        register(routes, 1, "task1.py", containerPath("lab1/task1.py"), workDir("lab1"));
        register(routes, 1, "task2.py", containerPath("lab1/task2.py"), workDir("lab1"));
        register(routes, 1, "trap.c", containerPath("lab1/trap.c"), workDir("lab1"));
        register(routes, 1, "timer.c", containerPath("lab1/timer.c"), workDir("lab1"));
        register(routes, 1, "trap2.c", containerPath("lab1/trap.c"), workDir("lab1"));

        // Lab2
        register(routes, 2, "task1.py", containerPath("lab2/task1.py"), workDir("lab2"));
        register(routes, 2, "task2.py", containerPath("lab2/task2.py"), workDir("lab2"));
        register(routes, 2, "kalloc.c", containerPath("lab2/kalloc.c"), workDir("lab2"));
        register(routes, 2, "vm.c", containerPath("lab2/vm.c"), workDir("lab2"));

        // Lab3
        register(routes, 3, "task1.py", containerPath("lab3/task1.py"), workDir("lab3"));
        register(routes, 3, "task2.py", containerPath("lab3/task2.py"), workDir("lab3"));
        register(routes, 3, "proc.c", containerPath("lab3/proc.c"), workDir("lab3"));

        // Lab4
        register(routes, 4, "task1.py", containerPath("lab4/task1.py"), workDir("lab4"));
        register(routes, 4, "task2.py", containerPath("lab4/task2.py"), workDir("lab4"));
        register(routes, 4, "fs.c", containerPath("lab4/fs.c"), workDir("lab4"));
        register(routes, 4, "bio.c", containerPath("lab4/bio.c"), workDir("lab4"));
        register(routes, 4, "pipe.c", containerPath("lab4/pipe.c"), workDir("lab4"));

        // Lab5
        register(routes, 5, "task1.py", containerPath("lab5/task1.py"), workDir("lab5"));
        register(routes, 5, "task2.py", containerPath("lab5/task2.py"), workDir("lab5"));
        register(routes, 5, "sync.c", containerPath("lab5/sync.c"), workDir("lab5"));

        return Map.copyOf(routes);
    }

    private static void register(Map<String, Map<String, JudgeRoute>> routes,
                                 int labId,
                                 String fileName,
                                 String containerPath,
                                 String workDir) {
        routes.computeIfAbsent(labKey(labId), key -> new HashMap<>())
                .put(normalizeFileName(fileName), new JudgeRoute(containerPath, workDir));
    }

    private static String containerPath(String relativePath) {
        return CONTAINER_BASE_PATH + "/" + relativePath;
    }

    private static String workDir(String relativePath) {
        return CONTAINER_BASE_PATH + "/" + relativePath;
    }

    private static String labKey(Integer labId) {
        return "lab" + labId;
    }

    private static String normalizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new BusinessException("任务文件名不能为空");
        }
        String normalized = fileName.trim().replace('\\', '/');
        return normalized.substring(normalized.lastIndexOf('/') + 1).toLowerCase(Locale.ROOT);
    }

    record JudgeRoute(String containerPath, String workDir) {
    }
}
