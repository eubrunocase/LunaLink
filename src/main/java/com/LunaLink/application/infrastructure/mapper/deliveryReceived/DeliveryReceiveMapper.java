package com.LunaLink.application.infrastructure.mapper.deliveryReceived;

import com.LunaLink.application.domain.model.delivery.DeliveryReceived;
import com.LunaLink.application.web.dto.DeliveryReceivedDTO.RequestDeliveryReceivedDTO;
import com.LunaLink.application.web.dto.DeliveryReceivedDTO.ResponseDeliveryReceivedDTO;

import java.util.List;

public interface DeliveryReceiveMapper {

    ResponseDeliveryReceivedDTO toDTO(DeliveryReceived deliveryReceived);
    DeliveryReceived toEntity(RequestDeliveryReceivedDTO responseDeliveryReceivedDTO);
    List<ResponseDeliveryReceivedDTO> toDTOList(List<DeliveryReceived> deliveryReceiveds);
}
