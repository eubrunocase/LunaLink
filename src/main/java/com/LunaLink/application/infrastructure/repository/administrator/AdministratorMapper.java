package com.LunaLink.application.infrastructure.repository.administrator;

import com.LunaLink.application.core.Administrator;
import com.LunaLink.application.web.dto.AdministratorDTO.AdmnistratorResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface AdministratorMapper {

    AdmnistratorResponseDTO toDTO(Administrator administrator);
    List<AdmnistratorResponseDTO> toDTOList(List<Administrator> administrators);
}
