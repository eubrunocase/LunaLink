package com.LunaLink.application.application.facades.delivery;

import com.LunaLink.application.application.ports.input.DeliveryReceivedServicePort;
import com.LunaLink.application.web.dto.DeliveryReceivedDTO.RequestDeliveryReceivedDTO;
import com.LunaLink.application.web.dto.DeliveryReceivedDTO.ResponseDeliveryReceivedDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class DeliveryReceivedFacade {

    private final DeliveryReceivedServicePort deliveryReceivedServicePort;

    public DeliveryReceivedFacade(DeliveryReceivedServicePort deliveryReceivedServicePort) {
        this.deliveryReceivedServicePort = deliveryReceivedServicePort;
    }

    public ResponseDeliveryReceivedDTO createDeliveryReceived(RequestDeliveryReceivedDTO request) {
        return deliveryReceivedServicePort.createDeliveryReceived(request);
    }

    public ResponseDeliveryReceivedDTO findDeliveryReceivedById(UUID id) {
        return deliveryReceivedServicePort.findDeliveryReceivedById(id);
    }

    public List<ResponseDeliveryReceivedDTO> findAllDeliveryReceiveds() {
        return deliveryReceivedServicePort.findAllDeliveryReceiveds();
    }

    public void deleteDeliveryReceived(UUID id) {
        deliveryReceivedServicePort.deleteDeliveryReceived(id);
    }

    public ResponseDeliveryReceivedDTO updateDeliveryReceived(UUID id, RequestDeliveryReceivedDTO request) {
        return deliveryReceivedServicePort.updateDeliveryReceived(id, request);
    }


}
