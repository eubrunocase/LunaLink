package com.LunaLink.application.application.facades.delivery;

import com.LunaLink.application.application.ports.input.DeliveryServicePort;
import com.LunaLink.application.web.dto.DeliveryDTO.RequestDeliveryDTO;
import com.LunaLink.application.web.dto.DeliveryDTO.ResponseDeliveryDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class DeliveryFacade {

    private final DeliveryServicePort deliveryService;

    public DeliveryFacade(DeliveryServicePort deliveryService) {
            this.deliveryService = deliveryService;
    }

    public ResponseDeliveryDTO createDelivery(RequestDeliveryDTO deliveryDTO) {
        return deliveryService.createDelivery(deliveryDTO);
    }

    public List<ResponseDeliveryDTO> findAllDeliveries() {
        return deliveryService.findAllDeliveries();
    }

    public ResponseDeliveryDTO findById(UUID id) {
        return deliveryService.findDeliveryById(id);
    }

    public ResponseDeliveryDTO updateDelivery(UUID id, RequestDeliveryDTO deliveryDTO) {
        return deliveryService.updateDelivery(id, deliveryDTO);
    }

    public ResponseDeliveryDTO confirmReceipt(UUID id, String pickedUpBy) {
        return deliveryService.confirmReceipt(id, pickedUpBy);
    }
}
