package com.LunaLink.application.infrastructure.repository.resident;

import com.LunaLink.application.core.Resident;
import com.LunaLink.application.web.dto.residentDTO.ResidentResponseDTO;

import java.util.List;

/*
  Abstração do repositório JPA

  Assinaturas precisam ser IDÊNTICAS as da JPA
 */

public interface ResidentServicePort {

    Resident findResidentByLogin (String login);
    Resident createResident (Resident resident);
    void deleteResident (Long id);
    Resident updateResident (Long id, Resident resident);
    List<ResidentResponseDTO> findAllResidents();
}
