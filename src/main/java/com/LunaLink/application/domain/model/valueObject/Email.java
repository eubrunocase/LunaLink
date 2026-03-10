package com.LunaLink.application.domain.model.valueObject;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Embeddable
@Getter
@EqualsAndHashCode
public class Email {

    private String address;

    public Email() {
    }

    public Email(String address) {
        if (address == null || !address.contains("@")) {
            throw new IllegalArgumentException("Email inválido: " + address);
        }
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return address;
    }
}
