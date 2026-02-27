package com.LunaLink.application.domain.events.deliveryEvents;

import java.time.LocalDateTime;
import java.util.UUID;

public class DeliveryReceivedEvent {

    private final UUID deliveryId;
    private final UUID userId;
    private final String protocolNumber;
    private final LocalDateTime receivedAt;

    public DeliveryReceivedEvent(UUID deliveryId, UUID userId, String protocolNumber, LocalDateTime receivedAt) {
        this.deliveryId = deliveryId;
        this.userId = userId;
        this.protocolNumber = protocolNumber;
        this.receivedAt = receivedAt;
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

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }
}
