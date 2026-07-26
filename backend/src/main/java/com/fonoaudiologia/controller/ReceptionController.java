package com.fonoaudiologia.controller;

import com.fonoaudiologia.dto.ReceptionRequest;
import com.fonoaudiologia.entity.ReceptionRecord;
import com.fonoaudiologia.entity.User;
import com.fonoaudiologia.repository.ReceptionRecordRepository;
import com.fonoaudiologia.repository.ConsultationRepository;
import com.fonoaudiologia.service.AuditService;
import com.fonoaudiologia.service.ReceptionService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reception")
public class ReceptionController {

    private final ReceptionService receptionService;
    private final AuditService auditService;
    private final ReceptionRecordRepository receptionRecordRepository;
    private final ConsultationRepository consultationRepository;

    public ReceptionController(ReceptionService receptionService, AuditService auditService,
                               ReceptionRecordRepository receptionRecordRepository,
                               ConsultationRepository consultationRepository) {
        this.receptionService = receptionService;
        this.auditService = auditService;
        this.receptionRecordRepository = receptionRecordRepository;
        this.consultationRepository = consultationRepository;
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            List<ReceptionRecord> list;
            if (startDate != null || endDate != null) {
                java.time.LocalDateTime start = startDate != null ? java.time.LocalDate.parse(startDate).atStartOfDay() : java.time.LocalDateTime.of(2000, 1, 1, 0, 0);
                java.time.LocalDateTime end = endDate != null ? java.time.LocalDate.parse(endDate).atTime(23, 59, 59) : java.time.LocalDateTime.now().plusYears(1);
                list = receptionRecordRepository.findByCreatedAtBetween(start, end);
            } else {
                list = receptionRecordRepository.findAll();
            }
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new java.util.HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ReceptionRequest request, HttpServletRequest httpRequest) {
        try {
            User user = auditService.getCurrentUser();
            ReceptionRecord record = receptionService.create(request, user != null ? user.getId() : null);
            auditService.log("CREATE", "RECEPTION", record.getId(),
                    "Registro de recepcao: " + record.getType(), httpRequest.getRemoteAddr());
            return ResponseEntity.ok(record);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new java.util.HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        try {
            ReceptionRecord record = receptionRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registro nao encontrado"));
            String status = body.get("status");
            if (status != null) {
                record.setStatus(status);
                receptionRecordRepository.save(record);
            }
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new java.util.HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }

    @GetMapping("/ready")
    public ResponseEntity<?> findReadyForConsultation(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            List<ReceptionRecord> checkins;
            if (startDate != null || endDate != null) {
                java.time.LocalDateTime start = startDate != null ? java.time.LocalDate.parse(startDate).atStartOfDay() : java.time.LocalDateTime.of(2000, 1, 1, 0, 0);
                java.time.LocalDateTime end = endDate != null ? java.time.LocalDate.parse(endDate).atTime(23, 59, 59) : java.time.LocalDateTime.now().plusYears(1);
                checkins = receptionRecordRepository.findByCreatedAtBetween(start, end).stream()
                        .filter(r -> ("CHECKIN".equals(r.getType()) || "WALKIN".equals(r.getType())) && r.getPatient() != null
                                && !"ATENDIDO".equals(r.getStatus()) && !"CANCELADO".equals(r.getStatus()))
                        .collect(Collectors.toList());
            } else {
                checkins = receptionRecordRepository.findAll()
                        .stream()
                        .filter(r -> ("CHECKIN".equals(r.getType()) || "WALKIN".equals(r.getType())) && r.getPatient() != null
                                && !"ATENDIDO".equals(r.getStatus()) && !"CANCELADO".equals(r.getStatus()))
                        .collect(Collectors.toList());
            }
            List<Long> activeRecordIds = consultationRepository.findReceptionRecordIdsWithActiveConsultations();
            List<ReceptionRecord> ready = checkins.stream()
                    .filter(r -> r.getPatient() != null && !activeRecordIds.contains(r.getId()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ready);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new java.util.HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }
}
