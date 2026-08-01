package com.fonoaudiologia.controller;

import com.fonoaudiologia.dto.PatientRequest;
import com.fonoaudiologia.entity.Patient;
import com.fonoaudiologia.service.AuditService;
import com.fonoaudiologia.service.PatientService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;
    private final AuditService auditService;

    public PatientController(PatientService patientService, AuditService auditService) {
        this.patientService = patientService;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<Patient>> findAll() {
        return ResponseEntity.ok(patientService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Patient> findById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.findById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Patient>> search(@RequestParam String q) {
        return ResponseEntity.ok(patientService.search(q));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody PatientRequest request, HttpServletRequest httpRequest) {
        try {
            Patient patient = patientService.create(request);
            auditService.log("CREATE", "PATIENT", patient.getId(),
                    "Paciente criado: " + patient.getName(), httpRequest.getRemoteAddr());
            return ResponseEntity.ok(patient);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new java.util.HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody PatientRequest request, HttpServletRequest httpRequest) {
        try {
            Patient patient = patientService.update(id, request);
            auditService.log("UPDATE", "PATIENT", patient.getId(),
                    "Paciente atualizado: " + patient.getName(), httpRequest.getRemoteAddr());
            return ResponseEntity.ok(patient);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new java.util.HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            Patient patient = patientService.findById(id);
            patientService.delete(id);
            auditService.log("DELETE", "PATIENT", id,
                    "Paciente excluído: " + patient.getName(), httpRequest.getRemoteAddr());
            return ResponseEntity.ok(new java.util.HashMap<String, Object>() {{ put("message", "Paciente excluído com sucesso"); }});
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new java.util.HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }
}
