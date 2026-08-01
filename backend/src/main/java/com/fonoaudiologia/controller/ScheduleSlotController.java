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
                                                          @RequestParam(required = false) Long unitId) {
        if (unitId != null) {
            return ResponseEntity.ok(service.findForUnitAndDate(unitId, LocalDate.parse(date)));
        }
        return ResponseEntity.ok(service.findForDate(LocalDate.parse(date)));
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
            ScheduleSlot slot = service.findById(id);
            int occupied;
            if (date != null && !date.isEmpty()) {
                occupied = service.countActiveAppointmentsForSlotOnDate(id, LocalDate.parse(date));
            } else {
                occupied = service.countActiveAppointmentsForSlot(id);
            }
            int remaining = slot.getCapacity() - occupied;
            HashMap<String, Object> result = new HashMap<>();
            result.put("capacity", slot.getCapacity());
            result.put("occupied", occupied);
            result.put("remaining", remaining);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new java.util.HashMap<String, Object>() {{ put("message", e.getMessage()); }});
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
