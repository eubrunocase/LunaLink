package com.LunaLink.application.web.dto.NotificationDTO;

public record PushSubscriptionRequestDTO(
        String endpoint,
        Keys keys
) {
    public record Keys(
            String p256dh,
            String auth
    ) {}
}
