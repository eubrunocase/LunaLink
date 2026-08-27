package com.LunaLink.application.web.dto.ReservationsDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record InspectionSubmitDTO(
        @JsonProperty("notes")
        String notes,

        @JsonProperty("items")
        @NotEmpty(message = "É obrigatório informar pelo menos um item de vistoria")
        @Valid
        List<InspectionItemDTO> items
) {

    public record InspectionItemDTO(
            @JsonProperty("equipmentName")
            @NotEmpty(message = "Nome do equipamento é obrigatório")
            String equipmentName,

            @JsonProperty("okConfirmed")
            boolean okConfirmed,

            @JsonProperty("photoUrl")
            @NotEmpty(message = "Foto do item é obrigatória")
            String photoUrl
    ) {
    }
}
