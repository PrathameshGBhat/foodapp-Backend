package com.notificationservice.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.notificationservice.Config.VendorNotifier;
import com.notificationservice.dto.VendorNotificationPayload;

@Service
public class NotificationService {

    private final VendorNotifier vendorNotifier;

    public NotificationService(VendorNotifier vendorNotifier) {
        this.vendorNotifier = vendorNotifier;
    }

    public void processOrderAndNotify(VendorNotificationPayload payload) {

        if (payload == null || payload.getVendorId() == null) {
            System.out.println("❌ Invalid notification payload");
            return;
        }

        try {
            // Prepare notification message
            Map<String, Object> data = new HashMap<>();
            data.put("orderId", payload.getOrderId());
            data.put("restaurantId", payload.getRestaurantId());
            data.put("subTotal", payload.getSubTotal());
            data.put("vendorId", payload.getVendorId());
            data.put("type", "ORDER_PLACED");

            // Send notification to vendor
            vendorNotifier.notifyVendor(
                    payload.getVendorId().toString(),
                    data
            );

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
