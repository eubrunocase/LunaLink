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

        // Primeira checagem de conflito = Já existe uma reserva na mesma data?
            if (reservationRepository.existsByDate(date)) {
                throw new IllegalStateException(
                        String.format("Já existe uma reserva para o dia %s. Não é possível criar outra.",
                                date));
            }
            
        // Segunda checagem de conflito = Outro usuário já reservou espaço na mesma data?
           if (reservationRepository.existsByDateAndSpace(date, s)) {
               throw new IllegalStateException(
                       String.format("O espaço '%s' já está reservado em %s.",
                               s.getType(), date));
           }
        // Terceira checagem de conflito = O mesmo usuário já fez uma reserva na mesma data e espaço?
            if (reservationRepository.existsByResidentAndDateAndSpace(r, date, s)) {
                throw new IllegalStateException(
                        String.format("O residente '%s' já possui reserva para '%s' em %s.",
                                r.getLogin(), s.getType(), date));
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

    public Reservation findReservationById(Long id) {
        return reservationRepository.findReservationById(id);
    }

}
