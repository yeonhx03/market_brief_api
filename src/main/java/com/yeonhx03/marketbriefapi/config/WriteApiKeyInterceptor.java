package com.yeonhx03.marketbriefapi.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

final class WriteApiKeyInterceptor implements HandlerInterceptor {

    static final String HEADER_NAME = "X-Market-Brief-Key";

    private final String expectedApiKey;

    WriteApiKeyInterceptor(String expectedApiKey) {
        this.expectedApiKey = expectedApiKey;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {
        if (!"POST".equals(request.getMethod()) || expectedApiKey.isBlank()) {
            return true;
        }

        var providedApiKey = request.getHeader(HEADER_NAME);

        if (matches(providedApiKey)) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("""
                {"code":"UNAUTHORIZED","message":"Valid write API key required"}
                """.strip());
        return false;
    }

    private boolean matches(String providedApiKey) {
        if (providedApiKey == null) {
            return false;
        }

        return MessageDigest.isEqual(
                expectedApiKey.getBytes(StandardCharsets.UTF_8),
                providedApiKey.getBytes(StandardCharsets.UTF_8)
        );
    }
}
