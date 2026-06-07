package com.LunaLink.application.web.dto.SpaceDTO;

import com.LunaLink.application.domain.enums.SpaceType;
import com.fasterxml.jackson.annotation.JsonProperty;

public record SpaceResponseDTO(@JsonProperty("id") Long id,
                               @JsonProperty("type") SpaceType type) {
}
