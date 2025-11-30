package com.cognizant.paymentservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for individual order items
 */
public class OrderItemDto {
    
    @JsonProperty("menuItemId")
    private Long menuItemId;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("unitPrice")
    private Double unitPrice;
    
    @JsonProperty("quantity")
    private Integer quantity;
    
    @JsonProperty("itemTotal")
    private Double itemTotal;
    
    @JsonProperty("specialInstructions")
    private String specialInstructions;

    // ==================== CONSTRUCTORS ====================

    public OrderItemDto() {}

    public OrderItemDto(Long menuItemId, String name, Double unitPrice, Integer quantity) {
        this.menuItemId = menuItemId;
        this.name = name;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.itemTotal = unitPrice * quantity;
    }

    public OrderItemDto(Long menuItemId, String name, Double unitPrice, Integer quantity, Double itemTotal) {
        this.menuItemId = menuItemId;
        this.name = name;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.itemTotal = itemTotal;
    }

    // ==================== GETTERS & SETTERS ====================

    public Long getMenuItemId() { 
        return menuItemId; 
    }
    public void setMenuItemId(Long menuItemId) { 
        this.menuItemId = menuItemId; 
    }

    public String getName() { 
        return name; 
    }
    public void setName(String name) { 
        this.name = name; 
    }

    public String getDescription() { 
        return description; 
    }
    public void setDescription(String description) { 
        this.description = description; 
    }

    public Double getUnitPrice() { 
        return unitPrice; 
    }
    public void setUnitPrice(Double unitPrice) { 
        this.unitPrice = unitPrice; 
    }

    public Integer getQuantity() { 
        return quantity; 
    }
    public void setQuantity(Integer quantity) { 
        this.quantity = quantity; 
    }

    public Double getItemTotal() { 
        return itemTotal; 
    }
    public void setItemTotal(Double itemTotal) { 
        this.itemTotal = itemTotal; 
    }

    public String getSpecialInstructions() { 
        return specialInstructions; 
    }
    public void setSpecialInstructions(String specialInstructions) { 
        this.specialInstructions = specialInstructions; 
    }

    // ==================== UTILITY METHODS ====================

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

    /**
     * Calculate item total
     */
    public Double calculateTotal() {
        return unitPrice * quantity;
    }
}