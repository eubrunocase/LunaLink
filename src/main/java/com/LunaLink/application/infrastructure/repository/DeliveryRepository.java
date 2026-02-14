package com.LunaLink.application.infrastructure.repository;

import com.LunaLink.application.application.ports.output.DeliveryRepositoryPort;
import com.LunaLink.application.domain.model.delivery.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, UUID>, DeliveryRepositoryPort {

    Delivery findDeliveryById(UUID id);
}
