package com.LunaLink.application.web.dto.DeliveryDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResponseDeliveryDTO(@JsonProperty("id") @Valid UUID Id,
                                  @JsonProperty("user") @Valid UUID userId,
                                  @JsonProperty("protocolNumber") @Valid String protocolNumber,
                                  @JsonProperty("image") byte[] image,
                                  @JsonProperty("createdAt") @Valid LocalDateTime createdAt,
                                  @JsonProperty("otherRecipient") String otherRecipient) {
}
