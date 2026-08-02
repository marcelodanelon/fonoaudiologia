package com.fonoaudiologia.controller;

import com.fonoaudiologia.dto.ScheduleSlotRequest;
import com.fonoaudiologia.entity.ScheduleSlot;
import com.fonoaudiologia.service.ScheduleSlotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/schedule-slots")
public class ScheduleSlotController {

    private final ScheduleSlotService service;

    public ScheduleSlotController(ScheduleSlotService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ScheduleSlot>> findAll(@RequestParam(required = false) Long unitId) {
        if (unitId != null) {
            return ResponseEntity.ok(service.findByUnit(unitId));
        }
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<ScheduleSlot>> findByDate(@PathVariable String date,
                                                          @RequestParam(required = false) Long unitId,
                                                          @RequestParam(required = false) Long professionalId) {
        if (unitId != null && professionalId != null) {
            return ResponseEntity.ok(service.findForUnitAndProfessionalAndDate(unitId, professionalId, LocalDate.parse(date)));
        }
        if (unitId != null) {
            return ResponseEntity.ok(service.findForUnitAndDate(unitId, LocalDate.parse(date)));
        }
        return ResponseEntity.ok(service.findForDate(LocalDate.parse(date)));
    }

    @GetMapping("/available-dates")
    public ResponseEntity<List<LocalDate>> availableDates(@RequestParam(required = false) Long unitId,
                                                          @RequestParam(required = false) Long professionalId,
                                                          @RequestParam(required = false) String from,
                                                          @RequestParam(required = false) String to) {
        LocalDate start = (from != null && !from.isEmpty()) ? LocalDate.parse(from) : null;
        LocalDate end = (to != null && !to.isEmpty()) ? LocalDate.parse(to) : null;
        return ResponseEntity.ok(service.findAvailableDates(unitId, professionalId, start, end));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.findById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<?> availability(@PathVariable Long id, @RequestParam(required = false) String date) {
        try {
            LocalDate parsedDate = (date != null && !date.isEmpty()) ? LocalDate.parse(date) : null;
            return ResponseEntity.ok(service.getAvailability(id, parsedDate));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ScheduleSlotRequest request) {
        try {
            return ResponseEntity.ok(service.create(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ScheduleSlotRequest request) {
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
            return ResponseEntity.ok(new HashMap<String, String>() {{ put("message", "Horário removido"); }});
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{ put("message", e.getMessage()); }});
        }
    }
}
