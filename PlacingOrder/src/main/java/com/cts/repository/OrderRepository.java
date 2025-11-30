package com.cts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cts.entities.Orders;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {

}
