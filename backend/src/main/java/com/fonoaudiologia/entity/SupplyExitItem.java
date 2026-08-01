package com.fonoaudiologia.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "supply_exit_items")
public class SupplyExitItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "exit_id", nullable = false)
    @JsonIgnore
    private SupplyExit exit;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "supply_id", nullable = false)
    private Supply supply;

    @Column(nullable = false)
    private Double quantity;

    public SupplyExitItem() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SupplyExit getExit() { return exit; }
    public void setExit(SupplyExit exit) { this.exit = exit; }
    public Supply getSupply() { return supply; }
    public void setSupply(Supply supply) { this.supply = supply; }
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
}
