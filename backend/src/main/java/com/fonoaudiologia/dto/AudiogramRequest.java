package com.fonoaudiologia.dto;

public class AudiogramRequest {
    private Long id;
    private Long consultationId;
    private Integer right250, right500, right1000, right2000, right3000, right4000, right6000, right8000;
    private Integer left250, left500, left1000, left2000, left3000, left4000, left6000, left8000;
    private String hearingLossType;
    private String observations;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getConsultationId() { return consultationId; }
    public void setConsultationId(Long consultationId) { this.consultationId = consultationId; }
    public Integer getRight250() { return right250; }
    public void setRight250(Integer right250) { this.right250 = right250; }
    public Integer getRight500() { return right500; }
    public void setRight500(Integer right500) { this.right500 = right500; }
    public Integer getRight1000() { return right1000; }
    public void setRight1000(Integer right1000) { this.right1000 = right1000; }
    public Integer getRight2000() { return right2000; }
    public void setRight2000(Integer right2000) { this.right2000 = right2000; }
    public Integer getRight3000() { return right3000; }
    public void setRight3000(Integer right3000) { this.right3000 = right3000; }
    public Integer getRight4000() { return right4000; }
    public void setRight4000(Integer right4000) { this.right4000 = right4000; }
    public Integer getRight6000() { return right6000; }
    public void setRight6000(Integer right6000) { this.right6000 = right6000; }
    public Integer getRight8000() { return right8000; }
    public void setRight8000(Integer right8000) { this.right8000 = right8000; }
    public Integer getLeft250() { return left250; }
    public void setLeft250(Integer left250) { this.left250 = left250; }
    public Integer getLeft500() { return left500; }
    public void setLeft500(Integer left500) { this.left500 = left500; }
    public Integer getLeft1000() { return left1000; }
    public void setLeft1000(Integer left1000) { this.left1000 = left1000; }
    public Integer getLeft2000() { return left2000; }
    public void setLeft2000(Integer left2000) { this.left2000 = left2000; }
    public Integer getLeft3000() { return left3000; }
    public void setLeft3000(Integer left3000) { this.left3000 = left3000; }
    public Integer getLeft4000() { return left4000; }
    public void setLeft4000(Integer left4000) { this.left4000 = left4000; }
    public Integer getLeft6000() { return left6000; }
    public void setLeft6000(Integer left6000) { this.left6000 = left6000; }
    public Integer getLeft8000() { return left8000; }
    public void setLeft8000(Integer left8000) { this.left8000 = left8000; }
    public String getHearingLossType() { return hearingLossType; }
    public void setHearingLossType(String hearingLossType) { this.hearingLossType = hearingLossType; }
    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
}
