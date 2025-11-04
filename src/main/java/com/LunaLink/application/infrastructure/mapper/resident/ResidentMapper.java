package com.LunaLink.application.infrastructure.mapper.resident;

import com.LunaLink.application.domain.model.resident.Resident;
import com.LunaLink.application.web.dto.residentDTO.ResidentResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface ResidentMapper {

    ResidentResponseDTO toDTO(Resident resident);
    List<ResidentResponseDTO> toDTOList(List<Resident> residents);
}
