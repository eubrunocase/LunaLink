package com.LunaLink.application.infrastructure.repository.resident;

import com.LunaLink.application.core.Resident;
import com.LunaLink.application.web.dto.residentDTO.ResidentResponseDTO;

import java.util.List;
import java.util.Optional;


public interface ResidentRepositoryPort {

    Resident findByLogin(String login);
    Resident save(Resident resident);
    void deleteById(Long id);
    List<Resident> findAll();
    Optional<Resident> findById(Long id);
}
