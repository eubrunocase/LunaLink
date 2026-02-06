package com.LunaLink.application.application.ports.output;

import com.LunaLink.application.domain.model.resident.Resident;

import java.util.List;
import java.util.Optional;


public interface ResidentRepositoryPort {

    Resident findByLogin(String login);
    Optional<Resident> findResidentByLogin(String login);
    Resident save(Resident resident);
    void deleteById(Long id);
    List<Resident> findAll();
    Optional<Resident> findById(Long id);
}
