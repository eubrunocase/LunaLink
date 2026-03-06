package com.LunaLink.application.web.dto.UserDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record UserSummaryDTO(
        @JsonProperty("id") UUID id,
        @JsonProperty("login") String login
) {}
