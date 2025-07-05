package com.LunaLink.application.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record ReservationRequest(@JsonProperty("resident") Long residentId,
                                 @JsonProperty("date") LocalDate date,
                                 @JsonProperty("space") Long spaceId) {
}
