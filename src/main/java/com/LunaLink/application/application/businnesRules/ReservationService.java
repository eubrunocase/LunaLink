package com.LunaLink.application.application.businnesRules;

import com.LunaLink.application.core.Reservation;
import com.LunaLink.application.core.Resident;
import com.LunaLink.application.core.Space;
import com.LunaLink.application.infrastructure.repository.ReservationRepository;
import com.LunaLink.application.infrastructure.repository.ResidentRepository;
import com.LunaLink.application.infrastructure.repository.SpaceRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationService {

    private final ResidentRepository residentRepository;
    private final SpaceRepository spaceRepository;
    private final ReservationRepository reservationRepository;

    public ReservationService(ResidentRepository residentRepository, SpaceRepository spaceRepository, ReservationRepository reservationRepository) {
        this.residentRepository = residentRepository;
        this.spaceRepository = spaceRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public Reservation createReservation(Long residentId, LocalDate date, Long spaceId) throws Exception {
        try {
        Resident r = residentRepository.findById(residentId)
                .orElseThrow(() -> new IllegalArgumentException("ERRO NO METODO CreateReservation da classe de service, Resident not found"));
        Space s = spaceRepository.findSpaceById(spaceId)
                .orElseThrow(() -> new IllegalArgumentException("ERRO NO METODO CreateReservation da classe de service, space not found"));

        if (reservationRepository.existsByResidentAndDateAndSpace(r, date, s)) {
            throw new Exception(
                    String.format("já existe uma reserva de % para % ", s.getType(), date, r.getLogin())

            );
        }

        Reservation reservation = new Reservation();
        reservation.setDate(date);
        reservation.assignTo(r, s);
        return reservationRepository.save(reservation);

        } catch (Exception e) {
            throw new Exception("Erro ao criar reserva: " + e.getMessage());
        }
    }

    public List<Reservation> findAllReservations() {
        return reservationRepository.findAll();
    }

}
