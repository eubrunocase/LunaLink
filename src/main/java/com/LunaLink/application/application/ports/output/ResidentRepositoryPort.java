package com.LunaLink.application.core.ports.output;

import com.LunaLink.application.core.domain.Resident;

import java.util.List;
import java.util.Optional;


public interface ResidentRepositoryPort {

    Resident findByLogin(String login);
    Resident save(Resident resident);
    void deleteById(Long id);
    List<Resident> findAll();
    Optional<Resident> findById(Long id);
}
