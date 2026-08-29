package com.LunaLink.application.web.dto.AnnouncementDTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResponseAnnouncementDTO(
        @JsonProperty("id") UUID Id,
        @JsonProperty("title") String Title,
        @JsonProperty("content") String Content,
        @JsonProperty("createdAt") LocalDateTime CreatedAt
) {
}
