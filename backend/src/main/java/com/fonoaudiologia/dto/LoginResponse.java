package com.fonoaudiologia.dto;

import java.util.List;

public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private String name;
    private String roleName;
    private List<String> permissions;
    private long sessionTimeoutMinutes;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
    public long getSessionTimeoutMinutes() { return sessionTimeoutMinutes; }
    public void setSessionTimeoutMinutes(long sessionTimeoutMinutes) { this.sessionTimeoutMinutes = sessionTimeoutMinutes; }
}
