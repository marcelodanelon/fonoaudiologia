package com.fonoaudiologia.dto;

import java.util.List;

public class DashboardStatsResponse {
    private long totalPatients;
    private long totalConsultations;
    private long consultationsThisMonth;
    private long pendingReception;
    private List<MonthlyCount> monthlyData;

    public long getTotalPatients() { return totalPatients; }
    public void setTotalPatients(long totalPatients) { this.totalPatients = totalPatients; }
    public long getTotalConsultations() { return totalConsultations; }
    public void setTotalConsultations(long totalConsultations) { this.totalConsultations = totalConsultations; }
    public long getConsultationsThisMonth() { return consultationsThisMonth; }
    public void setConsultationsThisMonth(long consultationsThisMonth) { this.consultationsThisMonth = consultationsThisMonth; }
    public long getPendingReception() { return pendingReception; }
    public void setPendingReception(long pendingReception) { this.pendingReception = pendingReception; }
    public List<MonthlyCount> getMonthlyData() { return monthlyData; }
    public void setMonthlyData(List<MonthlyCount> monthlyData) { this.monthlyData = monthlyData; }

    public static class MonthlyCount {
        private String month;
        private long consultations;

        public MonthlyCount() {}

        public MonthlyCount(String month, long consultations) {
            this.month = month;
            this.consultations = consultations;
        }

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        public long getConsultations() { return consultations; }
        public void setConsultations(long consultations) { this.consultations = consultations; }
    }
}
