package com.LunaLink.application.web.dto.SpaceDTO;


import java.time.LocalDate;
import java.util.List;

public record SpaceResponseDTO(Long spaceId,
                               String month,
                               List<LocalDate> unavailableDates) {

}
