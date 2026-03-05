package com.LunaLink.application.web.dto.DeliveryDTO;

import com.LunaLink.application.domain.enums.DeliveryStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResponseDeliveryDTO(@JsonProperty("id") UUID Id,
                                  @JsonProperty("user") UUID userId,
                                  @JsonProperty("protocolNumber") String protocolNumber,
                                  @JsonProperty("discrimination") String discrimination,
                                  @JsonProperty("image") byte[] image,
                                  @JsonProperty("createdAt") LocalDateTime createdAt,
                                  @JsonProperty("otherRecipient") String otherRecipient,
                                  @JsonProperty("status") DeliveryStatus status,
                                  @JsonProperty("deliveredAt") LocalDateTime deliveredAt,
                                  @JsonProperty("pickedUpBy") String pickedUpBy) {
}
