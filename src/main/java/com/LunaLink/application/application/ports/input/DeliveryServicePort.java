package com.LunaLink.application.application.ports.input;

import com.LunaLink.application.web.dto.DeliveryDTO.RequestDeliveryDTO;
import com.LunaLink.application.web.dto.DeliveryDTO.ResponseDeliveryDTO;

import java.util.List;
import java.util.UUID;

public interface DeliveryServicePort {

    ResponseDeliveryDTO createDelivery(RequestDeliveryDTO requestDeliveryDTO);
    ResponseDeliveryDTO findDeliveryById(UUID id);
    List<ResponseDeliveryDTO> findAllDeliveries();
    void deleteDelivery(UUID id);
    ResponseDeliveryDTO updateDelivery(UUID id, RequestDeliveryDTO requestDeliveryDTO);

}
