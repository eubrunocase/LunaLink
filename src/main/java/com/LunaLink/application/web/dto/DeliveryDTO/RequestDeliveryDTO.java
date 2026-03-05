package com.LunaLink.application.web.dto.DeliveryDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RequestDeliveryDTO(
                                 @JsonProperty("user") @NotNull UUID userId,
                                 @JsonProperty("protocolNumber") String protocolNumber,
                                 @JsonProperty("discrimination") String discrimination,
                                 @JsonProperty("image") byte[] image,
                                 @JsonProperty("otherRecipient") String otherRecipient) {
}
