package com.fonoaudiologia.dto;

public class ConsultationRequest {
    private Long id;
    private Long patientId;
    private Long professionalId;
    private Long unitId;
    private String type;
    private String status;
    private String chiefComplaint;
    private String anamnesis;
    private String clinicalHistory;
    private String physicalExam;
    private String diagnosis;
    private String conduct;
    private String observations;
    private Long receptionRecordId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public Long getProfessionalId() { return professionalId; }
    public void setProfessionalId(Long professionalId) { this.professionalId = professionalId; }
    public Long getUnitId() { return unitId; }
    public void setUnitId(Long unitId) { this.unitId = unitId; }
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
}
