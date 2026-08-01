package com.fonoaudiologia.entity;

import javax.persistence.*;

@Entity
@Table(name = "supply_stocks", uniqueConstraints = @UniqueConstraint(columnNames = {"supply_id", "unit_id"}))
public class SupplyStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "supply_id", nullable = false)
    private Supply supply;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "unit_id", nullable = false)
    private ServiceUnit unit;

    @Column(nullable = false)
    private Double quantity = 0.0;

    public SupplyStock() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Supply getSupply() { return supply; }
    public void setSupply(Supply supply) { this.supply = supply; }
    public ServiceUnit getUnit() { return unit; }
    public void setUnit(ServiceUnit unit) { this.unit = unit; }
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
}
