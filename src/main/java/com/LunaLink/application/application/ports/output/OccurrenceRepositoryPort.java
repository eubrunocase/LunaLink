package com.LunaLink.application.application.ports.output;

import com.LunaLink.application.domain.model.occurrence.Occurrence;
import com.LunaLink.application.domain.model.reservation.Reservation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OccurrenceRepositoryPort {
    Occurrence save(Occurrence occurrence);
    void deleteById(UUID uuid);
    Optional<Occurrence> findById(UUID uuid);
    List<Occurrence> findAll();

}
