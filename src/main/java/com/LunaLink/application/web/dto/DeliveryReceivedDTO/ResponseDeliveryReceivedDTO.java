package com.LunaLink.application.web.dto.DeliveryReceivedDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResponseDeliveryReceivedDTO(@JsonProperty("id") @Valid UUID id,
                                          @JsonProperty("deliveryId") @Valid UUID deliveryId,
                                          @JsonProperty("receivedAt") @Valid LocalDateTime receivedAt) {
}
