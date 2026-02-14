package com.LunaLink.application.web.dto.DeliveryDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.UUID;

public record RequestDeliveryDTO(
                                 @JsonProperty("user") @Valid UUID userId,
                                 @JsonProperty("protocolNumber") @Valid String protocolNumber,
                                 @JsonProperty("createdAt") @Valid LocalDateTime createdAt,
                                 @JsonProperty("image") byte[] image,
                                 @JsonProperty("otherRecipient") String otherRecipient) {
}
