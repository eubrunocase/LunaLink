package com.LunaLink.application.web.dto.ReservationsDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public record MonthlyReservationReportDTO(
        @JsonProperty("residentName") String residentName,
        @JsonProperty("apartment") String apartment,
        @JsonProperty("date") LocalDate date,
        @JsonProperty("spaceType") String spaceType
) {}
