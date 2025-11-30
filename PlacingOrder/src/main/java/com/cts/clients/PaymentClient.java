//package com.cts.clients;
//
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//
//
//@FeignClient(name = "payment-service", url = "http://localhost:8080") // Update URL/port if needed
//public interface PaymentClient {
//    @GetMapping("/api/payment/status/{orderId}")
//    String getPaymentStatus(@PathVariable("orderId") String orderId);
//
//	boolean processPayment(Long orderId, Double subTotal);
//}

package com.cts.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "payment-service", configuration = FeignClientInterceptor.class) // Update URL if needed
public interface PaymentClient {

    // Fetch payment status by orderId
    @GetMapping("/api/payment/status/{orderId}")
    String getPaymentStatus(@PathVariable("orderId") String orderId);

    // Process payment for an order
    @PostMapping("/api/payment/process")
    Boolean processPayment(@RequestParam("orderId") Long orderId,
                           @RequestParam("amount") Double subTotal);
}