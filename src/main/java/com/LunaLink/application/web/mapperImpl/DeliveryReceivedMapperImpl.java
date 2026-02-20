package com.LunaLink.application.web.mapperImpl;

import com.LunaLink.application.domain.model.delivery.DeliveryReceived;
import com.LunaLink.application.infrastructure.mapper.deliveryReceived.DeliveryReceiveMapper;
import com.LunaLink.application.web.dto.DeliveryReceivedDTO.RequestDeliveryReceivedDTO;
import com.LunaLink.application.web.dto.DeliveryReceivedDTO.ResponseDeliveryReceivedDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeliveryReceivedMapperImpl implements DeliveryReceiveMapper {

    @Override
    public ResponseDeliveryReceivedDTO toDTO(DeliveryReceived deliveryReceived) {
        if (deliveryReceived == null) {
            return null;
        }
        return new ResponseDeliveryReceivedDTO(
                deliveryReceived.getId(),
                deliveryReceived.getDeliveryId(),
                deliveryReceived.getReceivedAt()
        );
    }

    @Override
    public DeliveryReceived toEntity(RequestDeliveryReceivedDTO responseDeliveryReceivedDTO) {
        if (responseDeliveryReceivedDTO == null) {
            return null;
        }
            return new DeliveryReceived(
                    responseDeliveryReceivedDTO.deliveryId(),
                    responseDeliveryReceivedDTO.receivedAt()
            );
    }

    @Override
    public List<ResponseDeliveryReceivedDTO> toDTOList(List<DeliveryReceived> deliveryReceiveds) {
        if (deliveryReceiveds == null) {
            return null;
        }
        return deliveryReceiveds.stream()
                .map(this::toDTO)
                .toList();
    }
}
