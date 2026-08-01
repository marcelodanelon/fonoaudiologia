package com.fonoaudiologia.dto;

public class ReceptionRequest {
    private Long id;
    private Long patientId;
    private Long unitId;
    private String type; // CHECKIN, PHONE_CONTACT, WALKIN
    private String contactType; // TELEFONE, PORTA, AGENDAMENTO
    private String notes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public Long getUnitId() { return unitId; }
    public void setUnitId(Long unitId) { this.unitId = unitId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getContactType() { return contactType; }
    public void setContactType(String contactType) { this.contactType = contactType; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
