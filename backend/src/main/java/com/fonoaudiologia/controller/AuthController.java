package com.fonoaudiologia.controller;

import com.fonoaudiologia.dto.*;
import com.fonoaudiologia.entity.User;
import com.fonoaudiologia.security.JwtTokenProvider;
import com.fonoaudiologia.service.*;
import com.fonoaudiologia.service.AuditService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuditService auditService;
    private final SystemConfigService configService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(UserService userService, AuditService auditService,
                          SystemConfigService configService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.auditService = auditService;
        this.configService = configService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            LoginResponse response = userService.login(request.getUsername(), request.getPassword());
            auditService.log("LOGIN", "USER", response.getUserId(),
                    "Login realizado: " + request.getUsername(), httpRequest.getRemoteAddr());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest request) {
        String oldToken = request.getToken();
        if (oldToken == null || oldToken.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Token obrigatório"));
        }

        try {
            long timeUntilExpiration = jwtTokenProvider.getTimeUntilExpiration(oldToken);
            if (timeUntilExpiration == 0) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Token inválido"));
            }

            long refreshWindow = 24L * 60 * 60 * 1000;
            if (timeUntilExpiration > refreshWindow) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Token ainda válido, refresh não necessário"));
            }
            if (timeUntilExpiration < -refreshWindow) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Token expirado"));
            }

            Long userId = jwtTokenProvider.getUserIdFromTokenAllowExpired(oldToken);
            String username = jwtTokenProvider.getUsernameFromTokenAllowExpired(oldToken);
            String newToken = jwtTokenProvider.generateToken(userId, username);

            RefreshResponse response = new RefreshResponse();
            response.setToken(newToken);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Token inválido"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        User user = auditService.getCurrentUser();
        if (user == null) return ResponseEntity.status(401).build();

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRoleId(user.getRole().getId());
        response.setRoleName(user.getRole().getName());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/session-timeout")
    public ResponseEntity<?> getSessionTimeout() {
        long timeout = configService.getSessionTimeoutMinutes();
        return ResponseEntity.ok(new TimeoutResponse(timeout));
    }

    public static class ErrorResponse {
        private String message;
        public ErrorResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
    }

    public static class TimeoutResponse {
        private long timeoutMinutes;
        public TimeoutResponse(long timeoutMinutes) { this.timeoutMinutes = timeoutMinutes; }
        public long getTimeoutMinutes() { return timeoutMinutes; }
    }

    public static class RefreshRequest {
        private String token;
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }

    public static class RefreshResponse {
        private String token;
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }
}
