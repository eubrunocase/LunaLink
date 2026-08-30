package com.LunaLink.application.application.ports.input;

import com.LunaLink.application.web.dto.DeliveryDTO.RequestDeliveryDTO;
import com.LunaLink.application.web.dto.DeliveryDTO.ResponseDeliveryDTO;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface DeliveryServicePort {

    ResponseDeliveryDTO createDelivery(RequestDeliveryDTO requestDeliveryDTO);
    ResponseDeliveryDTO findDeliveryById(UUID id);
    List<ResponseDeliveryDTO> findAllDeliveries();
    List<ResponseDeliveryDTO> findDeliveriesByUserId(UUID userId);
    void deleteDelivery(UUID id);
    ResponseDeliveryDTO updateDelivery(UUID id, RequestDeliveryDTO requestDeliveryDTO);
    ResponseDeliveryDTO confirmReceipt(UUID deliveryId, String pickedUpBy);
    Map<String, String> generateUploadData(UUID userId, String fileName);
    String generateDownloadUrl(UUID deliveryId);

}
