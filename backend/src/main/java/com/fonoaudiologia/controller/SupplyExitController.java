package com.fonoaudiologia.controller;

import com.fonoaudiologia.dto.SupplyExitRequest;
import com.fonoaudiologia.entity.SupplyExit;
import com.fonoaudiologia.entity.User;
import com.fonoaudiologia.service.AuditService;
import com.fonoaudiologia.service.SupplyExitService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/supply-exits")
public class SupplyExitController {

    private final SupplyExitService service;
    private final AuditService auditService;

    public SupplyExitController(SupplyExitService service, AuditService auditService) {
        this.service = service;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<SupplyExit>> findAll(@RequestParam(required = false) Long unitId) {
        return ResponseEntity.ok(service.findAll(unitId));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody SupplyExitRequest request, HttpServletRequest httpRequest) {
        try {
            User user = auditService.getCurrentUser();
            SupplyExit saved = service.create(request, user);
            auditService.log("CREATE", "SUPPLY_EXIT", saved.getId(),
                    "Saída de insumos registrada (" + saved.getItems().size() + " item(ns))",
                    httpRequest.getRemoteAddr());
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody SupplyExitRequest request, HttpServletRequest httpRequest) {
        try {
            User user = auditService.getCurrentUser();
            SupplyExit saved = service.update(id, request, user);
            auditService.log("UPDATE", "SUPPLY_EXIT", saved.getId(),
                    "Saída de insumos alterada (" + saved.getItems().size() + " item(ns))",
                    httpRequest.getRemoteAddr());
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            service.delete(id);
            auditService.log("DELETE", "SUPPLY_EXIT", id, "Saída de insumos excluida",
                    httpRequest.getRemoteAddr());
            return ResponseEntity.ok(new HashMap<String, Object>() {{ put("message", "Saída excluida"); }});
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }
}
