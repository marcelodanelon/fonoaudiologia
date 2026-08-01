package com.fonoaudiologia.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "supply_entry_items")
public class SupplyEntryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entry_id", nullable = false)
    @JsonIgnore
    private SupplyEntry entry;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "supply_id", nullable = false)
    private Supply supply;

    @Column(nullable = false)
    private Double quantity;

    public SupplyEntryItem() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SupplyEntry getEntry() { return entry; }
    public void setEntry(SupplyEntry entry) { this.entry = entry; }
    public Supply getSupply() { return supply; }
    public void setSupply(Supply supply) { this.supply = supply; }
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
}
