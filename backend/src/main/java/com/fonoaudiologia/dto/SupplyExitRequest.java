package com.fonoaudiologia.dto;

import java.time.LocalDate;
import java.util.List;

public class SupplyExitRequest {
    private Long id;
    private Long unitId;
    private LocalDate exitDate;
    private Long patientId;
    private String notes;
    private List<InventoryItemRequest> items;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUnitId() { return unitId; }
    public void setUnitId(Long unitId) { this.unitId = unitId; }
    public LocalDate getExitDate() { return exitDate; }
    public void setExitDate(LocalDate exitDate) { this.exitDate = exitDate; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public List<InventoryItemRequest> getItems() { return items; }
    public void setItems(List<InventoryItemRequest> items) { this.items = items; }
}
