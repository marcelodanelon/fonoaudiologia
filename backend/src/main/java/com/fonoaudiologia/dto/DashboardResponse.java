package com.fonoaudiologia.dto;

import java.util.Map;

public class DashboardResponse {
    private long totalPatients;
    private long newPatientsThisMonth;
    private long totalConsultations;
    private long completedConsultations;
    private long scheduledConsultations;
    private long totalReceptions;
    private long phoneContacts;
    private long checkins;
    private long walkins;
    private long totalOperators;

    public long getTotalPatients() { return totalPatients; }
    public void setTotalPatients(long totalPatients) { this.totalPatients = totalPatients; }
    public long getNewPatientsThisMonth() { return newPatientsThisMonth; }
    public void setNewPatientsThisMonth(long newPatientsThisMonth) { this.newPatientsThisMonth = newPatientsThisMonth; }
    public long getTotalConsultations() { return totalConsultations; }
    public void setTotalConsultations(long totalConsultations) { this.totalConsultations = totalConsultations; }
    public long getCompletedConsultations() { return completedConsultations; }
    public void setCompletedConsultations(long completedConsultations) { this.completedConsultations = completedConsultations; }
    public long getScheduledConsultations() { return scheduledConsultations; }
    public void setScheduledConsultations(long scheduledConsultations) { this.scheduledConsultations = scheduledConsultations; }
    public long getTotalReceptions() { return totalReceptions; }
    public void setTotalReceptions(long totalReceptions) { this.totalReceptions = totalReceptions; }
    public long getPhoneContacts() { return phoneContacts; }
    public void setPhoneContacts(long phoneContacts) { this.phoneContacts = phoneContacts; }
    public long getCheckins() { return checkins; }
    public void setCheckins(long checkins) { this.checkins = checkins; }
    public long getWalkins() { return walkins; }
    public void setWalkins(long walkins) { this.walkins = walkins; }
    public long getTotalOperators() { return totalOperators; }
    public void setTotalOperators(long totalOperators) { this.totalOperators = totalOperators; }
}
