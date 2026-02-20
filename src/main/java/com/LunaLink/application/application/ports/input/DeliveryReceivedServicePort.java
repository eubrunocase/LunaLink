package com.LunaLink.application.application.ports.input;

import com.LunaLink.application.web.dto.DeliveryReceivedDTO.RequestDeliveryReceivedDTO;
import com.LunaLink.application.web.dto.DeliveryReceivedDTO.ResponseDeliveryReceivedDTO;

import java.util.List;
import java.util.UUID;

public interface DeliveryReceivedServicePort {

    ResponseDeliveryReceivedDTO createDeliveryReceived(RequestDeliveryReceivedDTO requestDeliveryReceivedDTO);
    ResponseDeliveryReceivedDTO findDeliveryReceivedById(UUID id);
    List<ResponseDeliveryReceivedDTO> findAllDeliveryReceiveds();
    void deleteDeliveryReceived(UUID id);
    ResponseDeliveryReceivedDTO updateDeliveryReceived(UUID id, RequestDeliveryReceivedDTO requestDeliveryReceivedDTO);
}
