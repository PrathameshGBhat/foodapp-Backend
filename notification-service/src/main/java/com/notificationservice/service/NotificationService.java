package com.notificationservice.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.notificationservice.Config.VendorNotifier;
import com.notificationservice.dto.OrdersDto;

@Service
public class NotificationService {

    private final RestaurantClient restaurantClient;
    private final VendorNotifier vendorNotifier;

    public NotificationService(RestaurantClient restaurantClient, VendorNotifier vendorNotifier) {
        this.restaurantClient = restaurantClient;
        this.vendorNotifier = vendorNotifier;
    }

    public void processOrderAndNotify(OrdersDto order) {

        if (order == null || order.getRestaurantId() == null) {
            System.out.println("❌ Invalid order");
            return;
        }

        try {
            var rest = restaurantClient.getRestaurantById(order.getRestaurantId());

            if (rest == null || rest.getVendorId() == null) {
                System.out.println("❌ Restaurant or vendor not found");
                return;
            }

            // FULL PAYLOAD now (vendor will see items)
            Map<String, Object> payload = new HashMap<>();
            payload.put("orderId", order.getOrderId());
            payload.put("restaurantId", order.getRestaurantId());
            payload.put("subTotal", order.getSubTotal());
            payload.put("createdAt", order.getCreatedAt());
            payload.put("type", "ORDER_PLACED");

            // 🔥 SEND ITEMS (actual list)
            payload.put("items", order.getItems());

            // optional (but useful)
            payload.put("itemsCount", order.getItems() != null ? order.getItems().size() : 0);

            vendorNotifier.notifyVendor(
                    String.valueOf(rest.getVendorId()),
                    payload
            );

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
