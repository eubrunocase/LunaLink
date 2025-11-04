package com.LunaLink.application.web.dto.checkOut_gym_DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

public record CheckOut_Gym_RequestDTO(@JsonProperty("checkinGymId") UUID CheckIn_Gym_Id,
                                      @JsonProperty("saida") LocalDateTime saida)  {
}
