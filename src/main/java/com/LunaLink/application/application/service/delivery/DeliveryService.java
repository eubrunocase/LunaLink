package com.LunaLink.application.application.service.delivery;

import com.LunaLink.application.application.ports.input.DeliveryServicePort;
import com.LunaLink.application.application.ports.output.DeliveryRepositoryPort;
import com.LunaLink.application.domain.model.delivery.Delivery;
import com.LunaLink.application.infrastructure.mapper.delivery.DeliveryMapper;
import com.LunaLink.application.web.dto.DeliveryDTO.RequestDeliveryDTO;
import com.LunaLink.application.web.dto.DeliveryDTO.ResponseDeliveryDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DeliveryService implements DeliveryServicePort {


    private final DeliveryRepositoryPort repository;
    private final DeliveryMapper mapper;

    public DeliveryService(DeliveryRepositoryPort repository, DeliveryMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ResponseDeliveryDTO createDelivery(RequestDeliveryDTO requestDeliveryDTO) {
         if (requestDeliveryDTO == null)
             throw new RuntimeException("MÉTODO CREATE Delivery DE DeliveryService: Delivery não pode ser nulo.");

         Delivery delivery = mapper.toEntity(requestDeliveryDTO);
         return mapper.toDTO(repository.save(delivery));
    }

    @Override
    public ResponseDeliveryDTO findDeliveryById(UUID id) {
        if (id == null) throw new RuntimeException("MÉTODO findDeliveryById USER DE DeliveryService: Delivery não pode ser nulo.");
        Delivery d = repository.findDeliveryById(id);
        return mapper.toDTO(d);
    }

    @Override
    public List<ResponseDeliveryDTO> findAllDeliveries() {
        List<Delivery> deliveries = repository.findAll();
        return mapper.toDTOList(deliveries);
    }

    @Override
    @Transactional
    public void deleteDelivery(UUID id) {
        if (id == null) throw new RuntimeException("MÉTODO deleteDelivery USER DE DeliveryService: Delivery não pode ser nulo.");
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public ResponseDeliveryDTO updateDelivery(UUID id, RequestDeliveryDTO requestDeliveryDTO) {
        Delivery deliveryForUpdate = repository.findDeliveryById(id);
        deliveryForUpdate.setProtocolNumber(requestDeliveryDTO.protocolNumber());
        deliveryForUpdate.setUserId(requestDeliveryDTO.userId());
        deliveryForUpdate.setImage(requestDeliveryDTO.image());
        deliveryForUpdate.setOtherRecipient(requestDeliveryDTO.otherRecipient());
        return mapper.toDTO(repository.save(deliveryForUpdate));
    }

}
