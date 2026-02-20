package com.LunaLink.application.application.facades.delivery;

import com.LunaLink.application.application.ports.input.DeliveryServicePort;
import com.LunaLink.application.web.dto.DeliveryDTO.RequestDeliveryDTO;
import com.LunaLink.application.web.dto.DeliveryDTO.ResponseDeliveryDTO;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;

@Component
public class DeliveryFacade {

    private final DeliveryServicePort deliveryServicePort;

    public DeliveryFacade(DeliveryServicePort deliveryServicePort) {
            this.deliveryServicePort = deliveryServicePort;
    }

    public ResponseDeliveryDTO findById(UUID id) {
        return deliveryServicePort.findDeliveryById(id);
    }

    public ResponseDeliveryDTO createDelivery(RequestDeliveryDTO request) {
        return deliveryServicePort.createDelivery(request);
    }

    public ResponseDeliveryDTO updateDelivery(UUID id, RequestDeliveryDTO request) {
        return deliveryServicePort.updateDelivery(id, request);
    }

    public void deleteDelivery(UUID id) {
        deliveryServicePort.deleteDelivery(id);
    }

    public List<ResponseDeliveryDTO> findAllDeliveries() {
        return deliveryServicePort.findAllDeliveries();
    }


}
