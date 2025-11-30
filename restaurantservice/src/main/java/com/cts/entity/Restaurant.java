package com.cts.entity;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "restaurants", uniqueConstraints = @UniqueConstraint(columnNames = "restaurantName"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long restaurantId;

    @Column(nullable = false, length = 100)
    private String restaurantName;

    @Column(nullable = false, length = 200)
    private String location;

    private LocalTime openTime;

    private LocalTime closeTime;

    @Column(nullable = false, length = 10)
    private String status;
    
    private Long vendorId;
}

