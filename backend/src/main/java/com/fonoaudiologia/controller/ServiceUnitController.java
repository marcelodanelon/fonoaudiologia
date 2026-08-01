package com.fonoaudiologia.controller;

import com.fonoaudiologia.entity.ServiceUnit;
import com.fonoaudiologia.service.AuditService;
import com.fonoaudiologia.service.ServiceUnitService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/service-units")
public class ServiceUnitController {

    private final ServiceUnitService service;
    private final AuditService auditService;

    public ServiceUnitController(ServiceUnitService service, AuditService auditService) {
        this.service = service;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<ServiceUnit>> findAll(
            @RequestParam(required = false) Boolean includeInactive) {
        if (Boolean.TRUE.equals(includeInactive)) {
            return ResponseEntity.ok(service.findAllIncludingInactive());
        }
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.findById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ServiceUnit unit, HttpServletRequest httpRequest) {
        try {
            ServiceUnit saved = service.create(unit);
            auditService.log("CREATE", "SERVICE_UNIT", saved.getId(),
                    "Unidade de atendimento criada: " + saved.getName(), httpRequest.getRemoteAddr());
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ServiceUnit unit, HttpServletRequest httpRequest) {
        try {
            ServiceUnit saved = service.update(id, unit);
            auditService.log("UPDATE", "SERVICE_UNIT", saved.getId(),
                    "Unidade de atendimento atualizada: " + saved.getName(), httpRequest.getRemoteAddr());
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            ServiceUnit unit = service.findById(id);
            service.delete(id);
            auditService.log("DELETE", "SERVICE_UNIT", id,
                    "Unidade de atendimento desativada: " + unit.getName(), httpRequest.getRemoteAddr());
            return ResponseEntity.ok(new HashMap<String, String>() {{ put("message", "Unidade de atendimento desativada"); }});
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }
}
