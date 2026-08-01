package com.fonoaudiologia.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "supply_exits")
public class SupplyExit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "unit_id", nullable = false)
    private ServiceUnit unit;

    @Column(nullable = false)
    private LocalDate exitDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    private String notes;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "operator_id")
    private User operator;

    @OneToMany(mappedBy = "exit", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SupplyExitItem> items = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public SupplyExit() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ServiceUnit getUnit() { return unit; }
    public void setUnit(ServiceUnit unit) { this.unit = unit; }
    public LocalDate getExitDate() { return exitDate; }
    public void setExitDate(LocalDate exitDate) { this.exitDate = exitDate; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public User getOperator() { return operator; }
    public void setOperator(User operator) { this.operator = operator; }
    public List<SupplyExitItem> getItems() { return items; }
    public void setItems(List<SupplyExitItem> items) { this.items = items; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
