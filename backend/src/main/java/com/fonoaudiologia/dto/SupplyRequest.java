package com.fonoaudiologia.dto;

public class SupplyRequest {
    private Long id;
    private String name;
    private String description;
    private String unitMeasure;
    private String category;
    private Double minimumQuantity;
    private boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getUnitMeasure() { return unitMeasure; }
    public void setUnitMeasure(String unitMeasure) { this.unitMeasure = unitMeasure; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Double getMinimumQuantity() { return minimumQuantity; }
    public void setMinimumQuantity(Double minimumQuantity) { this.minimumQuantity = minimumQuantity; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
