package com.example.oslab.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JudgeExecutionStateService {

    private final Map<Long, Integer> runningCounts = new ConcurrentHashMap<>();

    public void markRunning(Integer userId, Integer labId) {
        if (userId == null || labId == null) {
            return;
        }
        long key = toKey(userId, labId);
        runningCounts.compute(key, (unused, count) -> count == null ? 1 : count + 1);
    }

    public void clearRunning(Integer userId, Integer labId) {
        if (userId == null || labId == null) {
            return;
        }
        long key = toKey(userId, labId);
        runningCounts.compute(key, (unused, count) -> {
            if (count == null || count <= 1) {
                return null;
            }
            return count - 1;
        });
    }

    public boolean isRunning(Integer userId, Integer labId) {
        if (userId == null || labId == null) {
            return false;
        }
        return runningCounts.getOrDefault(toKey(userId, labId), 0) > 0;
    }

    private long toKey(Integer userId, Integer labId) {
        return (((long) userId) << 32) ^ (labId & 0xffffffffL);
    }
}
