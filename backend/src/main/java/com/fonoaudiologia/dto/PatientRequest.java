package com.fonoaudiologia.dto;

import java.time.LocalDate;

public class PatientRequest {
    private Long id;
    private String name;
    private String cpf;
    private String rg;
    private LocalDate birthDate;
    private String phone;
    private String phone2;
    private String email;
    private String address;
    private String city;
    private String state;
    private String observations;
    private boolean active;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getRg() { return rg; }
    public void setRg(String rg) { this.rg = rg; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPhone2() { return phone2; }
    public void setPhone2(String phone2) { this.phone2 = phone2; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
