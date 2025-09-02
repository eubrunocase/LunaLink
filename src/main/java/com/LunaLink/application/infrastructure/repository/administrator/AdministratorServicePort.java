package com.LunaLink.application.infrastructure.repository.administrator;

import com.LunaLink.application.core.Administrator;
import com.LunaLink.application.web.dto.AdministratorDTO.AdministratorResponseDTO;
import java.util.List;

public interface AdministratorServicePort {

    Administrator findAdmByLogin(String login);
    Administrator createAdministrator(Administrator administrator);
    void deleteAdministrator(Long id);
    Administrator updateAdministrator(Long id, Administrator administrator);
    List<AdministratorResponseDTO> findAllAdm ();

}
