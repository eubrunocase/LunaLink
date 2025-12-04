package com.LunaLink.application.domain.model.delivery;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Entity
@Table(name = "delivery")
@EqualsAndHashCode(of = "id")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID Id;
    @JsonProperty("nameResident")
    @Column(name = "nameResident")
    private String nameResident;
    @JsonProperty("protocolNumber")
    @Column(name = "protocolNumber")
    private String protocolNumber;
    @Lob
    @Column(name = "image", columnDefinition = "BYTEA")
    private byte[] image;

    public Delivery(String nameResident, String protocolNumber) {
        this.nameResident = nameResident;
        this.protocolNumber = protocolNumber;
    }

    public Delivery() {
    }

    public UUID getId() {
        return Id;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getNameResident() {
        return nameResident;
    }

    public String getProtocolNumber() {
        return protocolNumber;
    }

    public void setNameResident(String nameResident) {
        this.nameResident = nameResident;
    }

    public void setProtocolNumber(String protocolNumber) {
        this.protocolNumber = protocolNumber;
    }
}