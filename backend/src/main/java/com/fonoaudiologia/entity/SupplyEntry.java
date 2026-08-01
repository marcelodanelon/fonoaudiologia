package com.fonoaudiologia.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "supply_entries")
public class SupplyEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "unit_id", nullable = false)
    private ServiceUnit unit;

    @Column(nullable = false)
    private LocalDate entryDate;

    private String supplier;

    private String reference;

    private String notes;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "operator_id")
    private User operator;

    @OneToMany(mappedBy = "entry", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SupplyEntryItem> items = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public SupplyEntry() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ServiceUnit getUnit() { return unit; }
    public void setUnit(ServiceUnit unit) { this.unit = unit; }
    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }
    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public User getOperator() { return operator; }
    public void setOperator(User operator) { this.operator = operator; }
    public List<SupplyEntryItem> getItems() { return items; }
    public void setItems(List<SupplyEntryItem> items) { this.items = items; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
