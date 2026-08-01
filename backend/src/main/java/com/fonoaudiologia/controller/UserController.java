package com.fonoaudiologia.controller;

import com.fonoaudiologia.dto.UserRequest;
import com.fonoaudiologia.dto.UserResponse;
import com.fonoaudiologia.entity.Role;
import com.fonoaudiologia.repository.RoleRepository;
import com.fonoaudiologia.service.AuditService;
import com.fonoaudiologia.service.UserService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final RoleRepository roleRepository;
    private final AuditService auditService;

    public UserController(UserService userService, RoleRepository roleRepository, AuditService auditService) {
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody UserRequest request, HttpServletRequest httpRequest) {
        try {
            UserResponse response = userService.create(request);
            auditService.log("CREATE", "USER", response.getId(),
                    "Operador criado: " + response.getUsername(), httpRequest.getRemoteAddr());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new java.util.HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody UserRequest request, HttpServletRequest httpRequest) {
        try {
            UserResponse response = userService.update(id, request);
            auditService.log("UPDATE", "USER", response.getId(),
                    "Operador atualizado: " + response.getUsername(), httpRequest.getRemoteAddr());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new java.util.HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            userService.delete(id);
            auditService.log("DELETE", "USER", id,
                    "Operador desativado", httpRequest.getRemoteAddr());
            return ResponseEntity.ok(new java.util.HashMap<String, Object>() {{ put("message", "Operador desativado com sucesso"); }});
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new java.util.HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getRoles() {
        return ResponseEntity.ok(roleRepository.findAll());
    }

    @PutMapping("/roles/{id}")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest httpRequest) {
        try {
            Role role = roleRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));

            if (body.containsKey("description")) role.setDescription((String) body.get("description"));
            if (body.containsKey("canAccessDashboard")) role.setCanAccessDashboard((Boolean) body.get("canAccessDashboard"));
            if (body.containsKey("canAccessReception")) role.setCanAccessReception((Boolean) body.get("canAccessReception"));
            if (body.containsKey("canAccessConsultation")) role.setCanAccessConsultation((Boolean) body.get("canAccessConsultation"));
            if (body.containsKey("canAccessPatients")) role.setCanAccessPatients((Boolean) body.get("canAccessPatients"));
            if (body.containsKey("canAccessOperators")) role.setCanAccessOperators((Boolean) body.get("canAccessOperators"));
            if (body.containsKey("canAccessAuditLog")) role.setCanAccessAuditLog((Boolean) body.get("canAccessAuditLog"));
            if (body.containsKey("canAccessSystemConfig")) role.setCanAccessSystemConfig((Boolean) body.get("canAccessSystemConfig"));
            if (body.containsKey("canAccessInventory")) role.setCanAccessInventory((Boolean) body.get("canAccessInventory"));

            roleRepository.save(role);
            auditService.log("UPDATE", "ROLE", role.getId(),
                    "Perfis atualizados: " + role.getName(), httpRequest.getRemoteAddr());
            return ResponseEntity.ok(role);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new java.util.HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }
}
