package com.LunaLink.application.web.dto.DeliveryDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RequestDeliveryDTO(
                                 @JsonProperty("user") @NotNull UUID userId,
                                 @JsonProperty("protocolNumber") @NotNull String protocolNumber,
                                 @JsonProperty("image") byte[] image,
                                 @JsonProperty("otherRecipient") String otherRecipient) {
}
