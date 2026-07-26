package com.fonoaudiologia.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audiograms")
public class Audiogram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "consultation_id", nullable = false)
    private Consultation consultation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "professional_id", nullable = false)
    private User professional;

    // Right ear (OD - Orelha Direita) values in dB for each frequency
    private Integer right250;
    private Integer right500;
    private Integer right1000;
    private Integer right2000;
    private Integer right3000;
    private Integer right4000;
    private Integer right6000;
    private Integer right8000;

    // Left ear (OE - Orelha Esquerda) values in dB for each frequency
    private Integer left250;
    private Integer left500;
    private Integer left1000;
    private Integer left2000;
    private Integer left3000;
    private Integer left4000;
    private Integer left6000;
    private Integer left8000;

    // Type of hearing loss
    private String hearingLossType; // CONDUTIVA, MISTA, NEUROSENSORIAL, NORMAL

    private String observations;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Audiogram() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Consultation getConsultation() { return consultation; }
    public void setConsultation(Consultation consultation) { this.consultation = consultation; }
    public User getProfessional() { return professional; }
    public void setProfessional(User professional) { this.professional = professional; }
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
