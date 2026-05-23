package com.example.oslab.config;

import com.example.oslab.exception.BusinessException;
import com.example.oslab.service.JudgeExecutionStateService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class SubmissionIsolationInterceptor implements HandlerInterceptor {

    private static final String USER_ID_QUERY = "userId";
    private static final String LAB_ID_QUERY = "labId";

    private final JudgeExecutionStateService judgeExecutionStateService;

    public SubmissionIsolationInterceptor(JudgeExecutionStateService judgeExecutionStateService) {
        this.judgeExecutionStateService = judgeExecutionStateService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();
        if (!("/api/submissions".equals(uri) || uri.startsWith("/api/submissions/"))) {
            return true;
        }

        Integer userId = parseInteger(request.getParameter(USER_ID_QUERY));
        Integer labId = parseInteger(request.getParameter(LAB_ID_QUERY));

        if (judgeExecutionStateService.isRunning(userId, labId)) {
            response.setStatus(200);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":200,\"msg\":\"评测进行中，当前列表/详情请求已被阻断\",\"data\":[]}" );
            return false;
        }

        return true;
    }

    private Integer parseInteger(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

