package com.fonoaudiologia.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reception_records")
public class ReceptionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "operator_id", nullable = false)
    private User operator;

    @Column(nullable = false)
    private String type; // CHECKIN, PHONE_CONTACT, WALKIN

    private String contactType; // TELEFONE, PORTA, AGENDAMENTO

    private String status; // PENDENTE, ATENDIDO, CANCELADO

    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ReceptionRecord() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public User getOperator() { return operator; }
    public void setOperator(User operator) { this.operator = operator; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getContactType() { return contactType; }
    public void setContactType(String contactType) { this.contactType = contactType; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
