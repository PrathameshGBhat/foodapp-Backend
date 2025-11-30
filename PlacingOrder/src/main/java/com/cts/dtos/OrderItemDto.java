package com.cts.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
@Data
public class OrderItemDto {
    
    @JsonProperty("menuItemId")
    private Long menuItemId;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("unitPrice")
    private Double unitPrice;
    
    @JsonProperty("quantity")
    private Integer quantity;
    
    @JsonProperty("itemTotal")
    private Double itemTotal;

    // Getters and Setters
    public Long getMenuItemId() { return menuItemId; }
    public void setMenuItemId(Long menuItemId) { this.menuItemId = menuItemId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getItemTotal() { return itemTotal; }
    public void setItemTotal(Double itemTotal) { this.itemTotal = itemTotal; }

    @Override
    public String toString() {
        return "OrderItemDto{" +
                "menuItemId=" + menuItemId +
                ", name='" + name + '\'' +
                ", unitPrice=" + unitPrice +
                ", quantity=" + quantity +
                ", itemTotal=" + itemTotal +
                '}';
    }
}