package com.LunaLink.application.domain.model.delivery;
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
    private UUID Id;

    @JsonProperty("userId")
    @Column(name = "userId", nullable = false)
    private UUID userId;

    @JsonProperty("protocolNumber")
    @Column(name = "protocolNumber", nullable = false)
    private String protocolNumber;

    @JsonProperty("createdAt")
    @Column(name = "createdAt", nullable = true)
    private LocalDateTime createdAt;


    @Column(name = "image", columnDefinition = "BYTEA", nullable = true)
    private byte[] image;

    @JsonProperty("otherRecipient")
    @Column(name = "otherRecipient", nullable = true)
    private String otherRecipient;

    public Delivery(UUID userId, String protocolNumber, LocalDateTime createdAt ,byte[] image, String otherRecipient) {
        this.userId = userId;
        this.protocolNumber = protocolNumber;
        this.image = image;
        this.otherRecipient = otherRecipient;
    }

    public Delivery() {
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
    
    public UUID getId() {
        return Id;
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

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setProtocolNumber(String protocolNumber) {
        this.protocolNumber = protocolNumber;
    }

    public void setOtherRecipient(String otherRecipient) {
        this.otherRecipient = otherRecipient;
    }
}