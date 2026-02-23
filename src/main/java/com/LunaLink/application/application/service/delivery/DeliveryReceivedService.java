package com.LunaLink.application.application.service.delivery;

import com.LunaLink.application.application.ports.input.DeliveryReceivedServicePort;
import com.LunaLink.application.application.ports.output.DeliveryReceivedRepositoryPort;
import com.LunaLink.application.domain.model.delivery.DeliveryReceived;
import com.LunaLink.application.infrastructure.mapper.deliveryReceived.DeliveryReceiveMapper;
import com.LunaLink.application.web.dto.DeliveryReceivedDTO.RequestDeliveryReceivedDTO;
import com.LunaLink.application.web.dto.DeliveryReceivedDTO.ResponseDeliveryReceivedDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DeliveryReceivedService implements DeliveryReceivedServicePort {

    private final DeliveryReceivedRepositoryPort deliveryReceivedRepositoryPort;
    private final DeliveryReceiveMapper mapper;

    public DeliveryReceivedService(DeliveryReceivedRepositoryPort deliveryReceivedRepositoryPort, DeliveryReceiveMapper mapper) {
        this.deliveryReceivedRepositoryPort = deliveryReceivedRepositoryPort;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ResponseDeliveryReceivedDTO createDeliveryReceived(RequestDeliveryReceivedDTO requestDeliveryReceivedDTO) {
        if (requestDeliveryReceivedDTO == null) {
            throw new RuntimeException("MÉTODO CREATE DeliveryReceived DE DeliveryReceivedService: Delivery não pode ser nulo.");
        }
        DeliveryReceived deliveryReceived = mapper.toEntity(requestDeliveryReceivedDTO);
        return mapper.toDTO(deliveryReceivedRepositoryPort.save(deliveryReceived));
    }

    @Override
    public ResponseDeliveryReceivedDTO findDeliveryReceivedById(UUID id) {
        if (id == null) throw new RuntimeException("MÉTODO findDeliveryReceivedById de DeliveryReceivedService: Delivery não pode ser nulo.");
        DeliveryReceived deliveryReceived = deliveryReceivedRepositoryPort.findDeliveryReceivedById(id);
        return mapper.toDTO(deliveryReceived);
    }

    @Override
    public List<ResponseDeliveryReceivedDTO> findAllDeliveryReceiveds() {
        List<DeliveryReceived> deliveryReceiveds = deliveryReceivedRepositoryPort.findAll();
        return mapper.toDTOList(deliveryReceiveds);
    }

    @Override
    @Transactional
    public void deleteDeliveryReceived(UUID id) {
        if (id == null) throw new RuntimeException("MÉTODO findDeliveryReceivedById de DeliveryReceivedService: Delivery não pode ser nulo.");
        deliveryReceivedRepositoryPort.deleteById(id);
    }

    @Override
    @Transactional
    public ResponseDeliveryReceivedDTO updateDeliveryReceived(UUID id, RequestDeliveryReceivedDTO requestDeliveryReceivedDTO) {
        DeliveryReceived deliveryReceivedForUpdate = deliveryReceivedRepositoryPort.findDeliveryReceivedById(id);
        deliveryReceivedForUpdate.setReceivedAt(requestDeliveryReceivedDTO.receivedAt());
        return mapper.toDTO(deliveryReceivedRepositoryPort.save(deliveryReceivedForUpdate));
    }
}
