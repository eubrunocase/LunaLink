package com.LunaLink.application.application.service.facades;

import com.LunaLink.application.domain.model.administrator.Administrator;
import com.LunaLink.application.application.ports.input.AdministratorServicePort;
import com.LunaLink.application.web.dto.AdministratorDTO.AdministratorResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdministratorFacade {

    private final AdministratorServicePort administratorService;

    public AdministratorFacade(AdministratorServicePort administratorService) {
        this.administratorService = administratorService;
    }

    public Administrator findAdmByLogin (String login) {
        return administratorService.findAdmByLogin(login);
    }

    public Administrator createAdministrator (Administrator administrator) {
        return administratorService.createAdministrator(administrator);
    }

    public void deleteAdministrator (Long id) {
        administratorService.deleteAdministrator(id);
    }

    public Administrator updateAdministrator (Long id, Administrator administrator) {
        administrator.setId(id);
        return administratorService.updateAdministrator(id ,administrator);
    }

    public List<AdministratorResponseDTO> findAllAdm () {
        return administratorService.findAllAdm ();
    }

    public AdministratorResponseDTO findAdmById(Long id) {
         AdministratorResponseDTO adm = administratorService.findAdmById(id);
         return adm;
    }

}
