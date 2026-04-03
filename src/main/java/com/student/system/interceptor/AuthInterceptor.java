package com.student.system.interceptor;

import com.student.system.context.RequestContextHolder;
import com.student.system.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Skip OPTIONS (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorized(response, "未登录或token已过期");
            return false;
        }

        String token = authHeader.substring(7);
        try {
            if (jwtUtil.isExpired(token)) {
                sendUnauthorized(response, "token已过期");
                return false;
            }
            Long userId = jwtUtil.getUserId(token);
            String role = jwtUtil.getRole(token);
            String name = jwtUtil.getName(token);
            RequestContextHolder.set(userId, role, name);
            return true;
        } catch (Exception e) {
            sendUnauthorized(response, "无效的token");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        RequestContextHolder.clear();
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, String> body = new HashMap<>();
        body.put("message", message);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
