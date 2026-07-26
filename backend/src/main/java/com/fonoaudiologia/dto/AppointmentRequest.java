package com.fonoaudiologia.dto;

public class AppointmentRequest {
    private Long patientId;
    private Long professionalId;
    private Long scheduleSlotId;
    private String date;
    private String time;
    private String type;
    private String status;
    private String observations;

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public Long getProfessionalId() { return professionalId; }
    public void setProfessionalId(Long professionalId) { this.professionalId = professionalId; }
    public Long getScheduleSlotId() { return scheduleSlotId; }
    public void setScheduleSlotId(Long scheduleSlotId) { this.scheduleSlotId = scheduleSlotId; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
}
