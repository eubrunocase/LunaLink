package com.LunaLink.application.web.dto.checkOut_gym_DTO;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

public record CheckOut_Gym_ResponseDTO(@JsonProperty("id") UUID id,
                                       @JsonProperty("chekIn") CheckIn_Gym_SummaryDTO checkInGym,
                                       @JsonProperty("saida") LocalDateTime saida){

    public record CheckIn_Gym_SummaryDTO(
            @JsonProperty("id") UUID id
    ) {}


}
