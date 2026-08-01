package com.fonoaudiologia.controller;

import com.fonoaudiologia.dto.SupplyRequest;
import com.fonoaudiologia.entity.Supply;
import com.fonoaudiologia.service.AuditService;
import com.fonoaudiologia.service.SupplyService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/supplies")
public class SupplyController {

    private final SupplyService service;
    private final AuditService auditService;

    public SupplyController(SupplyService service, AuditService auditService) {
        this.service = service;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<Supply>> findAll(
            @RequestParam(required = false) Boolean includeInactive) {
        if (Boolean.TRUE.equals(includeInactive)) {
            return ResponseEntity.ok(service.findAllIncludingInactive());
        }
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/stock")
    public ResponseEntity<?> findStockByUnit(@RequestParam Long unitId) {
        try {
            return ResponseEntity.ok(service.findStocksByUnit(unitId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }

    @GetMapping("/{id}/stocks")
    public ResponseEntity<?> findStocksBySupply(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.findStocksBySupply(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
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
    public ResponseEntity<?> create(@RequestBody SupplyRequest request, HttpServletRequest httpRequest) {
        try {
            Supply saved = service.create(request);
            auditService.log("CREATE", "SUPPLY", saved.getId(),
                    "Insumo criado: " + saved.getName(), httpRequest.getRemoteAddr());
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody SupplyRequest request, HttpServletRequest httpRequest) {
        try {
            Supply saved = service.update(id, request);
            auditService.log("UPDATE", "SUPPLY", saved.getId(),
                    "Insumo atualizado: " + saved.getName(), httpRequest.getRemoteAddr());
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            Supply supply = service.findById(id);
            service.delete(id);
            auditService.log("DELETE", "SUPPLY", id,
                    "Insumo desativado: " + supply.getName(), httpRequest.getRemoteAddr());
            return ResponseEntity.ok(new HashMap<String, String>() {{ put("message", "Insumo desativado"); }});
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }
}
