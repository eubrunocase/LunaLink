package com.LunaLink.application.web.mapperImpl;

import com.LunaLink.application.domain.model.delivery.Delivery;
import com.LunaLink.application.infrastructure.mapper.delivery.DeliveryMapper;
import com.LunaLink.application.web.dto.DeliveryDTO.RequestDeliveryDTO;
import com.LunaLink.application.web.dto.DeliveryDTO.ResponseDeliveryDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DeliveryMapperImpl implements DeliveryMapper {

    @Override
    public ResponseDeliveryDTO toDTO(Delivery delivery) {
        if (delivery == null) {
            return null;
        }
        return new ResponseDeliveryDTO(delivery.getId(),
                                       delivery.getUserId(),
                                       delivery.getProtocolNumber(),
                                       delivery.getImage(),
                                       delivery.getCreatedAt(),
                                       delivery.getOtherRecipient());
    }

    @Override
    public Delivery toEntity(RequestDeliveryDTO deliveryDTO) {
        if (deliveryDTO == null) {
            return null;
        }
        return new Delivery(deliveryDTO.userId(),
                            deliveryDTO.protocolNumber(),
                            LocalDateTime.now(),
                            deliveryDTO.image(),
                            deliveryDTO.otherRecipient());
    }

    @Override
    public List<ResponseDeliveryDTO> toDTOList(List<Delivery> deliveries) {
        if (deliveries == null) {
            return null;
        }
        return deliveries.stream()
                .map(this::toDTO)
                .toList();
    }

}
