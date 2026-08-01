package com.fonoaudiologia.controller;

import com.fonoaudiologia.dto.SupplyEntryRequest;
import com.fonoaudiologia.entity.SupplyEntry;
import com.fonoaudiologia.entity.User;
import com.fonoaudiologia.service.AuditService;
import com.fonoaudiologia.service.SupplyEntryService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/supply-entries")
public class SupplyEntryController {

    private final SupplyEntryService service;
    private final AuditService auditService;

    public SupplyEntryController(SupplyEntryService service, AuditService auditService) {
        this.service = service;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<SupplyEntry>> findAll(@RequestParam(required = false) Long unitId) {
        return ResponseEntity.ok(service.findAll(unitId));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody SupplyEntryRequest request, HttpServletRequest httpRequest) {
        try {
            User user = auditService.getCurrentUser();
            SupplyEntry saved = service.create(request, user);
            auditService.log("CREATE", "SUPPLY_ENTRY", saved.getId(),
                    "Entrada de insumos registrada (" + saved.getItems().size() + " item(ns))",
                    httpRequest.getRemoteAddr());
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody SupplyEntryRequest request, HttpServletRequest httpRequest) {
        try {
            User user = auditService.getCurrentUser();
            SupplyEntry saved = service.update(id, request, user);
            auditService.log("UPDATE", "SUPPLY_ENTRY", saved.getId(),
                    "Entrada de insumos alterada (" + saved.getItems().size() + " item(ns))",
                    httpRequest.getRemoteAddr());
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }
}
