package com.LunaLink.application.infrastructure.mapper.delivery;

import com.LunaLink.application.domain.model.delivery.Delivery;
import com.LunaLink.application.web.dto.DeliveryDTO.RequestDeliveryDTO;
import com.LunaLink.application.web.dto.DeliveryDTO.ResponseDeliveryDTO;
import org.springframework.stereotype.Component;

import java.util.List;

public interface DeliveryMapper {

    ResponseDeliveryDTO toDTO(Delivery delivery);
    Delivery toEntity(RequestDeliveryDTO deliveryDTO);
    List<ResponseDeliveryDTO> toDTOList(List<Delivery> deliveries);
}
