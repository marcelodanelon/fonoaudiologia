package com.fonoaudiologia.dto;

public class InventoryItemRequest {
    private Long supplyId;
    private Double quantity;

    public Long getSupplyId() { return supplyId; }
    public void setSupplyId(Long supplyId) { this.supplyId = supplyId; }
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
}
