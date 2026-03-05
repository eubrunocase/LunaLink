package com.LunaLink.application.domain.model.delivery;
import com.LunaLink.application.domain.enums.DeliveryStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "delivery")
@EqualsAndHashCode(of = "id")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @JsonProperty("userId")
    @Column(name = "userId", nullable = false)
    private UUID userId;

    @JsonProperty("protocolNumber")
    @Column(name = "protocolNumber", nullable = true)
    private String protocolNumber;

    @JsonProperty("discrimination")
    @Column(name = "discrimination", nullable = true)
    private String discrimination;

    @JsonProperty("createdAt")
    @Column(name = "createdAt", nullable = true)
    private LocalDateTime createdAt;

    @Column(name = "image", columnDefinition = "BYTEA", nullable = true)
    private byte[] image;

    @JsonProperty("otherRecipient")
    @Column(name = "otherRecipient", nullable = true)
    private String otherRecipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    @Column(name = "deliveredAt")
    private LocalDateTime deliveredAt;

    @Column(name = "pickedUpBy")
    private String pickedUpBy;

    public Delivery(UUID userId, String protocolNumber, String discrimination, LocalDateTime createdAt ,byte[] image, String otherRecipient) {
        this.userId = userId;
        this.protocolNumber = protocolNumber;
        this.discrimination = discrimination;
        this.image = image;
        this.otherRecipient = otherRecipient;
        this.status = DeliveryStatus.PENDING;
    }

    public Delivery() {
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = DeliveryStatus.PENDING;
        }
    }

    public void markAsDelivered(String whoPickedUp) {
        this.status = DeliveryStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
        this.pickedUpBy = whoPickedUp;
    }
    
    public UUID getId() {
        return id;
    }

    public byte[] getImage() {
        return image;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getOtherRecipient() {
        return otherRecipient;
    }
    public void setImage(byte[] image) {
        this.image = image;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getProtocolNumber() {
        return protocolNumber;
    }

    public String getDiscrimination() {
        return discrimination;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setProtocolNumber(String protocolNumber) {
        this.protocolNumber = protocolNumber;
    }

    public void setDiscrimination(String discrimination) {
        this.discrimination = discrimination;
    }

    public void setOtherRecipient(String otherRecipient) {
        this.otherRecipient = otherRecipient;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(DeliveryStatus status) {
        this.status = status;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public String getPickedUpBy() {
        return pickedUpBy;
    }

    public void setPickedUpBy(String pickedUpBy) {
        this.pickedUpBy = pickedUpBy;
    }
}
