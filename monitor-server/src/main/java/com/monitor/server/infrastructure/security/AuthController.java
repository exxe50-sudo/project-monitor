package com.monitor.server.infrastructure.security;

import com.monitor.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ApiResponse<Map<String, String>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        // 简化认证：接受 admin/admin123
        if ("admin".equals(username) && "admin123".equals(password)) {
            String token = jwtTokenProvider.generateToken(username);
            return ApiResponse.success(Map.of("token", token, "username", username));
        }
        return ApiResponse.error(401, "Invalid credentials");
    }
}
