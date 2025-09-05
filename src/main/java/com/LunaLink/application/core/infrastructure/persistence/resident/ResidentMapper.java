package com.LunaLink.application.core.infrastructure.persistence.resident;

import com.LunaLink.application.core.domain.Resident;
import com.LunaLink.application.web.dto.residentDTO.ResidentResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface ResidentMapper {

    ResidentResponseDTO toDTO(Resident resident);
    List<ResidentResponseDTO> toDTOList(List<Resident> residents);
}
