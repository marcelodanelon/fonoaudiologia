package com.fonoaudiologia.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "consultations")
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "professional_id", nullable = false)
    private User professional;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "operator_id")
    private User operator;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "unit_id")
    private ServiceUnit unit;

    @Column(nullable = false)
    private String type; // CONSULTA, RETORNO, AVALIACAO

    @Column(nullable = false)
    private String status; // AGENDADA, EM_ANDAMENTO, CONCLUIDA, CANCELADA

    private String chiefComplaint; // Queixa principal

    private String anamnesis; // Anamnese

    private String clinicalHistory; // Histórico clinico

    private String physicalExam; // Exame fisico

    private String diagnosis; // Diagnóstico

    private String conduct; // Conduta

    private String observations; // Observações gerais

    private Long receptionRecordId; // Origem da recepção (nullable)

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Consultation() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public User getProfessional() { return professional; }
    public void setProfessional(User professional) { this.professional = professional; }
    public User getOperator() { return operator; }
    public void setOperator(User operator) { this.operator = operator; }
    public ServiceUnit getUnit() { return unit; }
    public void setUnit(ServiceUnit unit) { this.unit = unit; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getChiefComplaint() { return chiefComplaint; }
    public void setChiefComplaint(String chiefComplaint) { this.chiefComplaint = chiefComplaint; }
    public String getAnamnesis() { return anamnesis; }
    public void setAnamnesis(String anamnesis) { this.anamnesis = anamnesis; }
    public String getClinicalHistory() { return clinicalHistory; }
    public void setClinicalHistory(String clinicalHistory) { this.clinicalHistory = clinicalHistory; }
    public String getPhysicalExam() { return physicalExam; }
    public void setPhysicalExam(String physicalExam) { this.physicalExam = physicalExam; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getConduct() { return conduct; }
    public void setConduct(String conduct) { this.conduct = conduct; }
    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
    public Long getReceptionRecordId() { return receptionRecordId; }
    public void setReceptionRecordId(Long receptionRecordId) { this.receptionRecordId = receptionRecordId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
