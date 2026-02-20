package com.LunaLink.application.infrastructure.repository.deliveryReceived;

import com.LunaLink.application.application.ports.output.DeliveryReceivedRepositoryPort;
import com.LunaLink.application.domain.model.delivery.DeliveryReceived;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DeliveryReceivedRepository extends JpaRepository<DeliveryReceived, UUID>, DeliveryReceivedRepositoryPort {
    DeliveryReceived findDeliveryReceivedById(UUID id);
}
