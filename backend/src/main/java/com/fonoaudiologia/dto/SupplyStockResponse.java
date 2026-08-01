package com.fonoaudiologia.dto;

import com.fonoaudiologia.entity.ServiceUnit;
import com.fonoaudiologia.entity.Supply;
import com.fonoaudiologia.entity.SupplyStock;

public class SupplyStockResponse {

    private Long id;
    private Supply supply;
    private ServiceUnit unit;
    private Double quantity;
    private Double initialQuantity;
    private Double usedQuantity;

    public SupplyStockResponse() {}

    public SupplyStockResponse(SupplyStock stock, Double initialQuantity, Double usedQuantity) {
        this.id = stock.getId();
        this.supply = stock.getSupply();
        this.unit = stock.getUnit();
        this.quantity = stock.getQuantity();
        this.initialQuantity = initialQuantity;
        this.usedQuantity = usedQuantity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Supply getSupply() { return supply; }
    public void setSupply(Supply supply) { this.supply = supply; }
    public ServiceUnit getUnit() { return unit; }
    public void setUnit(ServiceUnit unit) { this.unit = unit; }
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public Double getInitialQuantity() { return initialQuantity; }
    public void setInitialQuantity(Double initialQuantity) { this.initialQuantity = initialQuantity; }
    public Double getUsedQuantity() { return usedQuantity; }
    public void setUsedQuantity(Double usedQuantity) { this.usedQuantity = usedQuantity; }
}
