package com.LunaLink.application.web.dto.AnnouncementDTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RequestAnnouncementDTO(
        @JsonProperty("title") String Title,
        @JsonProperty("content") String Content
) {
}
