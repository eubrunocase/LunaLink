package com.LunaLink.application.domain.model.delivery;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "deliveriesReceived")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class DeliveryReceived {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "deliveryId", nullable = false)
    @JsonProperty("deliveryId")
    private UUID deliveryId;

    @Column(name = "receivedAt", nullable = false)
    @JsonProperty("receivedAt")
    private LocalDateTime receivedAt;

    public DeliveryReceived(UUID deliveryId, LocalDateTime receivedAt) {
        this.deliveryId = deliveryId;
        this.receivedAt = receivedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getDeliveryId() {
        return deliveryId;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setDeliveryId(UUID deliveryId) {
        this.deliveryId = deliveryId;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    @Override
    public String toString() {
        return "DeliveryReceived{" +
                "id=" + id +
                ", deliveryId=" + deliveryId +
                ", receivedAt=" + receivedAt +
                '}';
    }
}
