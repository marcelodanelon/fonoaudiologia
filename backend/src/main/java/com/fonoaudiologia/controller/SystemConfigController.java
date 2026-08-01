package com.fonoaudiologia.controller;

import com.fonoaudiologia.dto.ConfigRequest;
import com.fonoaudiologia.entity.SystemConfig;
import com.fonoaudiologia.entity.User;
import com.fonoaudiologia.service.AuditService;
import com.fonoaudiologia.service.SystemConfigService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class SystemConfigController {

    private final SystemConfigService configService;
    private final AuditService auditService;

    public SystemConfigController(SystemConfigService configService, AuditService auditService) {
        this.configService = configService;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<SystemConfig>> findAll() {
        return ResponseEntity.ok(configService.findAll());
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody ConfigRequest request, HttpServletRequest httpRequest) {
        try {
            User user = auditService.getCurrentUser();
            SystemConfig config = configService.update(request.getConfigKey(), request.getConfigValue(), user);
            auditService.log("UPDATE", "CONFIG", config.getId(),
                    "Configuração atualizada: " + request.getConfigKey() + " = " + request.getConfigValue(),
                    httpRequest.getRemoteAddr());
            return ResponseEntity.ok(config);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new java.util.HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }
}
