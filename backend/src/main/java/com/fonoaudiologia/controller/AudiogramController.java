package com.fonoaudiologia.controller;

import com.fonoaudiologia.dto.AudiogramRequest;
import com.fonoaudiologia.entity.Audiogram;
import com.fonoaudiologia.entity.User;
import com.fonoaudiologia.service.AuditService;
import com.fonoaudiologia.service.AudiogramService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audiograms")
public class AudiogramController {

    private final AudiogramService audiogramService;
    private final AuditService auditService;

    public AudiogramController(AudiogramService audiogramService, AuditService auditService) {
        this.audiogramService = audiogramService;
        this.auditService = auditService;
    }

    @GetMapping("/consultation/{consultationId}")
    public ResponseEntity<List<Audiogram>> findByConsultation(@PathVariable Long consultationId) {
        return ResponseEntity.ok(audiogramService.findByConsultation(consultationId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Audiogram> findById(@PathVariable Long id) {
        return ResponseEntity.ok(audiogramService.findById(id));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody AudiogramRequest request, HttpServletRequest httpRequest) {
        try {
            User user = auditService.getCurrentUser();
            Audiogram audiogram = audiogramService.create(request, user != null ? user.getId() : null);
            auditService.log("CREATE", "AUDIOGRAM", audiogram.getId(),
                    "Audiograma registrado", httpRequest.getRemoteAddr());
            return ResponseEntity.ok(audiogram);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new java.util.HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody AudiogramRequest request, HttpServletRequest httpRequest) {
        try {
            Audiogram audiogram = audiogramService.update(id, request);
            auditService.log("UPDATE", "AUDIOGRAM", audiogram.getId(),
                    "Audiograma atualizado", httpRequest.getRemoteAddr());
            return ResponseEntity.ok(audiogram);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new java.util.HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }
}
