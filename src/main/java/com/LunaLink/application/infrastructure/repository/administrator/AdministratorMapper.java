package com.LunaLink.application.infrastructure.repository.administrator;

import com.LunaLink.application.core.Administrator;
import com.LunaLink.application.web.dto.AdministratorDTO.AdministratorResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface AdministratorMapper {

    AdministratorResponseDTO toDTO(Administrator administrator);
    List<AdministratorResponseDTO> toDTOList(List<Administrator> administrators);
}
