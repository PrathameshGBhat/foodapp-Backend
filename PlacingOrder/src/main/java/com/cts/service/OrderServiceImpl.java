package com.cts.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cts.clients.CartClients;
import com.cts.clients.InventoryClient;
import com.cts.clients.PaymentClient;
import com.cts.dtos.InventoryUpdateItem;
import com.cts.dtos.InventoryUpdateRequest;
import com.cts.dtos.OrderItemDto;
import com.cts.dtos.OrdersDto;
import com.cts.entities.CartItemResponse;
import com.cts.entities.CartResponse;
import com.cts.entities.OrderStatus;
import com.cts.entities.OrdersRequest;
import com.cts.entities.Orders;
import com.cts.repository.OrderRepository;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    
    private OrderRepository orderRepository;
    
    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    
    @Autowired
    private CartClients.CartClient cartClient;
    
    @Autowired
    private PaymentClient paymentClient;
    @Autowired
    private InventoryClient inventoryClient;

    @Override
    @Transactional(readOnly = true)
    public List<OrdersDto> getAllOrders() {
        logger.info("Fetching all orders from repository");
        List<Orders> entities = orderRepository.findAll();
        return entities.stream().map(this::convertToDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrdersDto getOrdersById(Long orderId) {
        logger.info("Fetching order by ID: {}", orderId);
        return orderRepository.findById(orderId)
            .map(this::convertToDto)
            .orElse(null);
    }

    @Override
    @Transactional
    public OrdersDto addOrders(OrdersRequest req) {
        Integer cartId = req.getCartId();
        if (cartId == null) {
            logger.error("❌ Cart ID is required");
            throw new IllegalArgumentException("cartId is required");
        }

        CartResponse cart = cartClient.getCartById(cartId);
        if (cart == null) {
            logger.error("❌ Cart not found for id: {}", cartId);
            throw new IllegalArgumentException("Cart not found for id: " + cartId);
        }

        Orders entity = new Orders();
        entity.setOrderStatus(OrderStatus.PLACED);
        entity.setCustomerId(req.getCustomerId());
        entity.setRestaurantId(req.getRestaurantId());
        entity.setCartId(cart.getCartId());
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        Double subtotal = null;
        try {
            subtotal = cart.getTotalCartPrice();
        } catch (Throwable t) {  }
        if (subtotal == null) {
            try { subtotal = cart.getTotalCartPrice(); } catch (Throwable t) {  }
        }
        entity.setSubTotal(subtotal != null ? subtotal : 0.0);

        if (cart.getItems() != null) {
            List<com.cts.entities.OrderItem> items = new ArrayList<>();
            for (CartItemResponse ci : cart.getItems()) {
                com.cts.entities.OrderItem oi = new com.cts.entities.OrderItem();
                oi.setMenuItemId(ci.getItemId());
                oi.setName(ci.getName());
                oi.setUnitPrice(ci.getPrice());
                Integer qty = ci.getQuantity();
                if (qty == null) {
                    if (ci.getTotalItemPrice() != null && ci.getPrice() != null && ci.getPrice() != 0) {
                        qty = (int) Math.round(ci.getTotalItemPrice() / ci.getPrice());
                        if (qty <= 0) qty = 1;
                    } else {
                        qty = 1;
                    }
                }
                oi.setQuantity(qty);
                if (ci.getTotalItemPrice() != null) {
                    oi.setItemTotal(ci.getTotalItemPrice());
                } else if (oi.getUnitPrice() != null) {
                    oi.setItemTotal(oi.getUnitPrice() * oi.getQuantity());
                } else {
                    oi.setItemTotal(0.0);
                }
                oi.setOrder(entity); 
                items.add(oi);
            }
            entity.setItems(items);
        }

        Orders saved = orderRepository.save(entity);
        logger.info("✅ Order saved with ID: {}, Status: PLACED", saved.getOrderId());

        return convertToDto(saved);
    }
    private InventoryUpdateRequest buildInventoryUpdateRequestFromOrder1(Orders order) {
    	 
        InventoryUpdateRequest req = new InventoryUpdateRequest();
     
        // ---------- FIX FOR restaurantId ----------
        Long restId = null;
        if (order.getRestaurantId() != null) {
            Object r = order.getRestaurantId();
     
            if (r instanceof Integer) {
                restId = ((Integer) r).longValue();
            } else if (r instanceof Long) {
                restId = (Long) r;
            } else {
                restId = Long.valueOf(r.toString());
            }
        }
        req.setRestaurantId(restId);
     
        // ---------- BUILD ITEMS ----------
        List<InventoryUpdateItem> items =
                (order.getItems() == null)
                        ? Collections.emptyList()
                        : order.getItems().stream()
                        .filter(oi -> oi != null && oi.getMenuItemId() != null && oi.getQuantity() != null)
                        .map(oi -> new InventoryUpdateItem(
                                Long.valueOf(oi.getMenuItemId().toString()),
                                oi.getQuantity()))
                        .collect(Collectors.toList());
     
        req.setItems(items);
     
        return req;
    }
    @Override
    @Transactional
    public OrdersDto updateOrderStatusByPayment(Long orderId, String paymentStatus) {
        logger.info("Auto-updating order: {}, paymentStatus: {}", orderId, paymentStatus);
     
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.error("Order not found for id: {}", orderId);
                    return new IllegalArgumentException("Order not found for id: " + orderId);
                });
     
        // map paymentStatus -> new order status
        OrderStatus newStatus;
        switch (paymentStatus.trim().toUpperCase()) {
            case "SUCCESS":
                newStatus = OrderStatus.CONFIRMED;
                break;
            case "FAILED":
            case "REFUNDED":
                newStatus = OrderStatus.CANCELLED;
                break;
            default:
                logger.warn("Unknown payment status: {}, keeping order as is", paymentStatus);
                newStatus = order.getOrderStatus();
        }
     
        String oldStatus = order.getOrderStatus().toString();
        order.setOrderStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
     
        // Save tentative status change now — we'll compensate if inventory fails
        Orders updated = orderRepository.save(order);
        logger.info("Order {} status updated: {} -> {}", orderId, oldStatus, newStatus);
     
        // If payment success -> reduce inventory for all items in order
        if (newStatus == OrderStatus.CONFIRMED) {
            try {
                // Build InventoryUpdateRequest from order items
                InventoryUpdateRequest invReq = buildInventoryUpdateRequestFromOrder1(order);
                logger.info("Calling Inventory service to update inventory for order {}: {}", orderId, invReq);
     
                ResponseEntity<Void> resp = inventoryClient.updateInventory(invReq);
                if (!resp.getStatusCode().is2xxSuccessful()) {
                    // treat as failure
                    throw new RuntimeException("Inventory service returned non-2xx: " + resp.getStatusCode());
                }
     
                logger.info("Inventory updated successfully for order {}", orderId);
                // final success: leave order as CONFIRMED (already saved)
            } catch (Exception ex) {
                // Compensate: inventory update failed -> cancel the order
                logger.error("Inventory update failed for order {} -> cancelling order. Reason: {}", orderId, ex.getMessage());
                order.setOrderStatus(OrderStatus.CANCELLED);
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
     
                // Optionally: throw, or return updated DTO. Here we throw to bubble up.
                throw new RuntimeException("Saga failed: inventory update failed. Order cancelled.", ex);
            }
        }
     
        return convertToDto(updated);
    }
     
    private InventoryUpdateRequest buildInventoryUpdateRequestFromOrder(Orders order) {
        InventoryUpdateRequest req = new InventoryUpdateRequest();
        req.setRestaurantId(order.getRestaurantId() == null ? null : order.getRestaurantId().longValue());
     
        List<InventoryUpdateItem> items = order.getItems().stream()
                .map(oi -> new InventoryUpdateItem(oi.getMenuItemId(), oi.getQuantity()))
                .collect(Collectors.toList());
     
        req.setItems(items);
        return req;
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId) {
        logger.info("Fetching payment status for order: {}", orderId);
        Orders order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found for id: " + orderId));

        try {
            // Step 1: Fetch payment status from Payment Service
            String paymentStatus = paymentClient.getPaymentStatus(orderId.toString());
            logger.info("Payment status from Payment Service: {}", paymentStatus);

            if (paymentStatus == null || paymentStatus.isBlank()) {
                logger.error("❌ Payment status is null or empty");
                throw new IllegalArgumentException("Payment status is null or empty");
            }

            // Step 2: Map payment status to order status
            OrderStatus mapped;
            switch (paymentStatus.trim().toUpperCase()) {
                case "SUCCESS":
                case "PLACED":
                    mapped = OrderStatus.PAYMENT_SUCCESS;
                    logger.info("Mapped to PAYMENT_SUCCESS");
                    break;
                case "FAILED":
                case "PAYMENT_FAILED":
                    mapped = OrderStatus.PAYMENT_FAILED;
                    logger.info("Mapped to PAYMENT_FAILED");
                    break;
                default:
                    logger.error("❌ Unknown payment status: {}", paymentStatus);
                    throw new IllegalArgumentException("Unknown payment status: " + paymentStatus);
            }

            // Step 3: Apply Saga logic
            if (mapped == OrderStatus.PAYMENT_SUCCESS) {
                // Confirm order
                order.setOrderStatus(OrderStatus.CONFIRMED);
                logger.info("✅ Order confirmed");
            } else {
                // Compensating transaction: cancel order
                order.setOrderStatus(OrderStatus.CANCELLED);
                logger.info("❌ Order cancelled due to payment failure");
            }

            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
            logger.info("✅ Order status updated successfully");

        } catch (Exception e) {
            // Saga failure handling
            logger.error("❌ Saga failed: {}", e.getMessage());
            order.setOrderStatus(OrderStatus.PAYMENT_FAILED);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
            throw new RuntimeException("Saga failed during order update", e);
        }
    }
    
    @Override
    @Transactional
    public void deleteOrder(Long orderId) {
        logger.info("Deleting order: {}", orderId);
        orderRepository.deleteById(orderId);
        logger.info("✅ Order deleted: {}", orderId);
    }
    
    /**
     * Helper method to convert Order entity to OrdersDto
     * Must be called within @Transactional context to access lazy collections
     */
    private OrdersDto convertToDto(Orders entity) {
        com.cts.dtos.OrdersDto dto = new com.cts.dtos.OrdersDto();
        dto.setOrderId(entity.getOrderId());
        dto.setOrderStatus(entity.getOrderStatus());
        dto.setCustomerId(entity.getCustomerId());
        dto.setCartId(entity.getCartId());
        dto.setRestaurantId(entity.getRestaurantId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setSubTotal(entity.getSubTotal());
        
        // ✅ Access lazy collection within transaction
        if (entity.getItems() != null) {
            for (com.cts.entities.OrderItem oi : entity.getItems()) {
                com.cts.dtos.OrderItemDto i = new com.cts.dtos.OrderItemDto();
                i.setMenuItemId(oi.getMenuItemId());
                i.setName(oi.getName());
                i.setUnitPrice(oi.getUnitPrice());
                i.setQuantity(oi.getQuantity());
                i.setItemTotal(oi.getItemTotal());
                dto.getItems().add(i);
            }
        }
        
        return dto;
    }
}