package com.LunaLink.application.application.ports.output;

import com.LunaLink.application.domain.model.delivery.Delivery;

import java.util.List;
import java.util.UUID;

public interface DeliveryRepositoryPort {

    Delivery save(Delivery delivery);
    void deleteById(UUID id);
    List<Delivery> findAll();
    Delivery findDeliveryById(UUID id);
}
