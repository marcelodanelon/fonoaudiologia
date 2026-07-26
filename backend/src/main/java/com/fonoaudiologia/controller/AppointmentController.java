package com.fonoaudiologia.controller;

import com.fonoaudiologia.dto.AppointmentRequest;
import com.fonoaudiologia.entity.Appointment;
import com.fonoaudiologia.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Appointment>> findAll(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        if (startDate != null && endDate != null) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            if (start.equals(end)) {
                return ResponseEntity.ok(service.findByDate(start));
            }
        }
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/scheduled/{date}")
    public ResponseEntity<List<Appointment>> findScheduledByDate(@PathVariable String date) {
        return ResponseEntity.ok(service.findScheduledByDate(LocalDate.parse(date)));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Appointment>> findByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(service.findByPatient(patientId));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody AppointmentRequest request) {
        try {
            return ResponseEntity.ok(service.create(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody AppointmentRequest request) {
        try {
            return ResponseEntity.ok(service.update(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.ok(new HashMap<String, String>() {{ put("message", "Agendamento cancelado"); }});
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }
}
