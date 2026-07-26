package com.fonoaudiologia.service;

import com.fonoaudiologia.dto.*;
import com.fonoaudiologia.entity.*;
import com.fonoaudiologia.repository.*;
import com.fonoaudiologia.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final SystemConfigRepository configRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider,
                       SystemConfigRepository configRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.configRepository = configRepository;
    }

    public LoginResponse login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario ou senha invalidos"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Usuario ou senha invalidos");
        }

        if (!user.isActive()) {
            throw new RuntimeException("Usuario desativado");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());

        List<String> permissions = new ArrayList<>();
        Role role = user.getRole();
        if (role.isCanAccessDashboard()) permissions.add("dashboard");
        if (role.isCanAccessReception()) permissions.add("reception");
        if (role.isCanAccessConsultation()) permissions.add("consultation");
        if (role.isCanAccessPatients()) permissions.add("patients");
        if (role.isCanAccessOperators()) permissions.add("operators");
        if (role.isCanAccessAuditLog()) permissions.add("auditLog");
        if (role.isCanAccessSystemConfig()) permissions.add("systemConfig");

        long timeout = 30;
        Optional<SystemConfig> cfg = configRepository.findByConfigKey("session_timeout_minutes");
        if (cfg.isPresent()) {
            timeout = Long.parseLong(cfg.get().getConfigValue());
        }

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setName(user.getName());
        response.setRoleName(role.getName());
        response.setPermissions(permissions);
        response.setSessionTimeoutMinutes(timeout);

        return response;
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public UserResponse findById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));
        return toResponse(user);
    }

    public UserResponse create(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Nome de usuario ja existe");
        }
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email ja cadastrado");
        }
        if (request.getCpf() != null && userRepository.existsByCpf(request.getCpf())) {
            throw new RuntimeException("CPF ja cadastrado");
        }
        validatePasswordStrength(request.getPassword());

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Perfil nao encontrado"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setCpf(request.getCpf());
        user.setPhone(request.getPhone());
        user.setRole(role);
        user.setActive(request.isActive());

        return toResponse(userRepository.save(user));
    }

    public UserResponse update(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setCpf(request.getCpf());
        user.setPhone(request.getPhone());
        user.setActive(request.isActive());

        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Perfil nao encontrado"));
            user.setRole(role);
        }

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            validatePasswordStrength(request.getPassword());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return toResponse(userRepository.save(user));
    }

    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));
        user.setActive(false);
        userRepository.save(user);
    }

    public long countActive() {
        return userRepository.findAll().stream().filter(User::isActive).count();
    }

    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new RuntimeException("A senha deve ter no minimo 8 caracteres");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new RuntimeException("A senha deve conter pelo menos uma letra maiuscula");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new RuntimeException("A senha deve conter pelo menos uma letra minuscula");
        }
        if (!password.matches(".*\\d.*")) {
            throw new RuntimeException("A senha deve conter pelo menos um numero");
        }
    }

    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setCpf(user.getCpf());
        response.setPhone(user.getPhone());
        response.setRoleId(user.getRole().getId());
        response.setRoleName(user.getRole().getName());
        response.setActive(user.isActive());
        return response;
    }
}
