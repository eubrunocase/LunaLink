package com.LunaLink.application.domain.events.deliveryEvents;

import java.time.LocalDateTime;
import java.util.UUID;

public class DeliveryCreatedEvent {

    private final UUID deliveryId;
    private final UUID userId;
    private final String protocolNumber;
    private final LocalDateTime createdAt;

    public DeliveryCreatedEvent(UUID deliveryId, UUID userId, String protocolNumber, LocalDateTime createdAt) {
        this.deliveryId = deliveryId;
        this.userId = userId;
        this.protocolNumber = protocolNumber;
        this.createdAt = createdAt;
    }

    public UUID getDeliveryId() {
        return deliveryId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getProtocolNumber() {
        return protocolNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
