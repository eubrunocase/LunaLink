package com.LunaLink.application.core.services.businnesRules.facades;

import com.LunaLink.application.core.domain.Resident;
import com.LunaLink.application.core.ports.input.ResidentServicePort;
import com.LunaLink.application.web.dto.residentDTO.ResidentResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ResidentFacade {

    private final ResidentServicePort residentServicePort;

    public ResidentFacade(ResidentServicePort residentServicePort) {
        this.residentServicePort = residentServicePort;
    }

    public Resident findResidentByLogin (String login) {
        return residentServicePort.findResidentByLogin(login);
    }

    public Resident createResident (Resident resident) {
        return residentServicePort.createResident(resident);
    }

    public void deleteResident (Long id) {
        residentServicePort.deleteResident(id);
    }

    public Resident updateResident (Long id, Resident resident) {
        return residentServicePort.updateResident(id, resident);
    }

    public ResidentResponseDTO findResidentById(Long id) {
        return residentServicePort.findResidentById(id);
    }

    public List<ResidentResponseDTO> findAllResidents() {
        return residentServicePort.findAllResidents();
    }
    
}
