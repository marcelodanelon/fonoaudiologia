package com.fonoaudiologia.dto;

import java.time.LocalDate;
import java.util.List;

public class SupplyEntryRequest {
    private Long id;
    private Long unitId;
    private LocalDate entryDate;
    private String supplier;
    private String reference;
    private String notes;
    private List<InventoryItemRequest> items;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUnitId() { return unitId; }
    public void setUnitId(Long unitId) { this.unitId = unitId; }
    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }
    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public List<InventoryItemRequest> getItems() { return items; }
    public void setItems(List<InventoryItemRequest> items) { this.items = items; }
}
