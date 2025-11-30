package com.cts.entity;

import org.hibernate.validator.constraints.UniqueElements;

import com.cts.client.RestaurantResponse;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "menu_items" ,uniqueConstraints=@UniqueConstraint(columnNames= {"itemName","restaurantId"}))
public class MenuItem {
		@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long itemId;
	    private String itemName;
	    private String description;
	    private Double price;
	    private Boolean isavailable;
	    private Long restaurantId;
	    @ManyToOne
	    @JoinColumn(name = "category_id", nullable = false)
	    private Category category;    
	}

