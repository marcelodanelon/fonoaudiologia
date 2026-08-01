package com.fonoaudiologia.entity;

import javax.persistence.*;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private boolean canAccessDashboard = false;

    @Column(nullable = false)
    private boolean canAccessReception = false;

    @Column(nullable = false)
    private boolean canAccessConsultation = false;

    @Column(nullable = false)
    private boolean canAccessPatients = false;

    @Column(nullable = false)
    private boolean canAccessOperators = false;

    @Column(nullable = false)
    private boolean canAccessAuditLog = false;

    @Column(nullable = false)
    private boolean canAccessSystemConfig = false;

    @Column
    private Boolean canAccessInventory = false;

    public Role() {}

    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public static Role admin() {
        Role r = new Role("ADMINISTRADOR", "Acesso total ao sistema");
        r.setCanAccessDashboard(true);
        r.setCanAccessReception(true);
        r.setCanAccessConsultation(true);
        r.setCanAccessPatients(true);
        r.setCanAccessOperators(true);
        r.setCanAccessAuditLog(true);
        r.setCanAccessSystemConfig(true);
        r.setCanAccessInventory(true);
        return r;
    }

    public static Role recepcionista() {
        Role r = new Role("RECEPCIONISTA", "Acesso a recepção e pacientes");
        r.setCanAccessDashboard(true);
        r.setCanAccessReception(true);
        r.setCanAccessPatients(true);
        return r;
    }

    public static Role fonoaudiólogo() {
        Role r = new Role("FONOAUDIOLOGO", "Acesso a consultas e audiogramas");
        r.setCanAccessDashboard(true);
        r.setCanAccessConsultation(true);
        r.setCanAccessPatients(true);
        return r;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isCanAccessDashboard() { return canAccessDashboard; }
    public void setCanAccessDashboard(boolean canAccessDashboard) { this.canAccessDashboard = canAccessDashboard; }
    public boolean isCanAccessReception() { return canAccessReception; }
    public void setCanAccessReception(boolean canAccessReception) { this.canAccessReception = canAccessReception; }
    public boolean isCanAccessConsultation() { return canAccessConsultation; }
    public void setCanAccessConsultation(boolean canAccessConsultation) { this.canAccessConsultation = canAccessConsultation; }
    public boolean isCanAccessPatients() { return canAccessPatients; }
    public void setCanAccessPatients(boolean canAccessPatients) { this.canAccessPatients = canAccessPatients; }
    public boolean isCanAccessOperators() { return canAccessOperators; }
    public void setCanAccessOperators(boolean canAccessOperators) { this.canAccessOperators = canAccessOperators; }
    public boolean isCanAccessAuditLog() { return canAccessAuditLog; }
    public void setCanAccessAuditLog(boolean canAccessAuditLog) { this.canAccessAuditLog = canAccessAuditLog; }
    public boolean isCanAccessSystemConfig() { return canAccessSystemConfig; }
    public void setCanAccessSystemConfig(boolean canAccessSystemConfig) { this.canAccessSystemConfig = canAccessSystemConfig; }
    public boolean isCanAccessInventory() { return canAccessInventory != null && canAccessInventory; }
    public void setCanAccessInventory(Boolean canAccessInventory) { this.canAccessInventory = canAccessInventory; }
}
