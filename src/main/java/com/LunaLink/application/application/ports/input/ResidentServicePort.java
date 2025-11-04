package com.LunaLink.application.application.ports.input;

import com.LunaLink.application.domain.model.resident.Resident;
import com.LunaLink.application.web.dto.residentDTO.ResidentResponseDTO;

import java.util.List;

/*
  Abstração do repositório JPA

 */

public interface ResidentServicePort {

    Resident findResidentByLogin (String login);
    Resident createResident (Resident resident);
    void deleteResident (Long id);
    Resident updateResident (Long id, Resident resident);
    List<ResidentResponseDTO> findAllResidents();
    ResidentResponseDTO findResidentById(Long id);
}
