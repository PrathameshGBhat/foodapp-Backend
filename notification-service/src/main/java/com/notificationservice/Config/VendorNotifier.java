package com.notificationservice.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class VendorNotifier {
	


    private final SimpMessagingTemplate template;

    public VendorNotifier(SimpMessagingTemplate template) {
        this.template = template;
    }

//     Sends payload to the vendor topic. Frontend should subscribe to /topic/vendor/{vendorId}
 
    public void notifyVendor(String vendorId, Object payload) {
        String destination = "/topic/vendor/" + vendorId;
        template.convertAndSend(destination, payload);
    }

}
