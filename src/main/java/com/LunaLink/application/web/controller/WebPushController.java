package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.service.notification.WebPushService;
import com.LunaLink.application.web.dto.NotificationDTO.PushSubscriptionRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/lunaLink/push")
public class WebPushController {

    private final WebPushService webPushService;

    public WebPushController(WebPushService webPushService) {
        this.webPushService = webPushService;
    }

    @GetMapping("/public-key")
    public ResponseEntity<Map<String, String>> getPublicKey() {
        return ResponseEntity.ok(Map.of("publicKey", webPushService.getPublicKey()));
    }

    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(@RequestBody PushSubscriptionRequestDTO subscription, Authentication authentication) {
        String userEmail = authentication.getName();
        webPushService.subscribe(userEmail, subscription);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<Void> unsubscribe(@RequestBody Map<String, String> payload) {
        String endpoint = payload.get("endpoint");
        if (endpoint != null) {
            webPushService.unsubscribe(endpoint);
        }
        return ResponseEntity.ok().build();
    }
}
