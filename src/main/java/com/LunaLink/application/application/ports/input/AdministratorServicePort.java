package com.LunaLink.application.core.ports.input;

import com.LunaLink.application.core.domain.Administrator;
import com.LunaLink.application.web.dto.AdministratorDTO.AdministratorResponseDTO;
import java.util.List;

public interface AdministratorServicePort {

    Administrator findAdmByLogin(String login);
    Administrator createAdministrator(Administrator administrator);
    void deleteAdministrator(Long id);
    Administrator updateAdministrator(Long id, Administrator administrator);
    List<AdministratorResponseDTO> findAllAdm ();
    AdministratorResponseDTO findAdmById(Long id);

}
