package com.LunaLink.application.application.ports.output;

import com.LunaLink.application.domain.model.delivery.DeliveryReceived;

import java.util.List;
import java.util.UUID;

public interface DeliveryReceivedRepositoryPort {

    DeliveryReceived save(DeliveryReceived deliveryReceived);
    void deleteById(UUID id);
    List<DeliveryReceived> findAll();
    DeliveryReceived findDeliveryReceivedById(UUID id);

}
