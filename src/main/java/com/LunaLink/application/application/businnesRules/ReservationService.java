package com.LunaLink.application.application.businnesRules;

import com.LunaLink.application.core.MonthlyReservations;
import com.LunaLink.application.core.Reservation;
import com.LunaLink.application.core.Resident;
import com.LunaLink.application.core.Space;
import com.LunaLink.application.infrastructure.repository.monthlyReservation.MonthlyReservationRepository;
import com.LunaLink.application.infrastructure.repository.reservation.ReservationMapper;
import com.LunaLink.application.infrastructure.repository.reservation.ReservationRepository;
import com.LunaLink.application.infrastructure.repository.resident.ResidentRepository;
import com.LunaLink.application.infrastructure.repository.space.SpaceRepository;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationRequestDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationResponseDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReservationService {

    private final ResidentRepository residentRepository;
    private final SpaceRepository spaceRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final MonthlyReservationRepository monthlyReservationRepository;

    public ReservationService(ResidentRepository residentRepository, SpaceRepository spaceRepository,
                              ReservationRepository reservationRepository,
                              ReservationMapper reservationMapper,
                              MonthlyReservationRepository monthlyReservationRepository) {
        this.residentRepository = residentRepository;
        this.spaceRepository = spaceRepository;
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
        this.monthlyReservationRepository = monthlyReservationRepository;
    }

    @Transactional
    public ReservationResponseDTO createReservation(ReservationRequestDTO data) throws Exception {
        try {
            Resident r = residentRepository.findById(data.residentId())
                    .orElseThrow(() -> new IllegalArgumentException("ERRO NO METODO CreateReservation da classe de service, Resident not found"));
            Space s = spaceRepository.findSpaceById(data.spaceId())
                    .orElseThrow(() -> new IllegalArgumentException("ERRO NO METODO CreateReservation da classe de service, space not found"));

            // Primeira checagem de conflito = Já existe uma reserva na mesma data?
            if (reservationRepository.existsByDate(data.date())) {
                throw new IllegalStateException(
                        String.format("Já existe uma reserva para o dia %s. Não é possível criar outra."));
            }

            // Segunda checagem de conflito = Outro usuário já reservou espaço na mesma data?
            if (reservationRepository.existsByDateAndSpace(data.date(), s)) {
                throw new IllegalStateException(
                        String.format("O espaço '%s' já está reservado em %s.",
                                s.getType(), data.date()));
            }
            // Terceira checagem de conflito = O mesmo usuário já fez uma reserva na mesma data e espaço?
            if (reservationRepository.existsByResidentAndDateAndSpace(r, data.date(), s)) {
                throw new IllegalStateException(
                        String.format("O residente '%s' já possui reserva para '%s' em %s.",
                                r.getLogin(), s.getType(), data.date()));
            }

            Reservation reservation = new Reservation();
            reservation.setDate(data.date());
            reservation.assignTo(r, s);
            Reservation savedReservation = reservationRepository.save(reservation);

            MonthlyReservations listReservations = new MonthlyReservations(r, savedReservation);

            LocalDateTime horaReserva = LocalDateTime.now();
            LocalDateTime horaReservaSemNanos = horaReserva.truncatedTo(ChronoUnit.SECONDS);

            listReservations.setCreationDate(horaReservaSemNanos);

            monthlyReservationRepository.save(listReservations);
            return reservationMapper.toDto(savedReservation);

        } catch (Exception e) {
            throw new Exception("Erro ao criar reserva: " + e.getMessage());
        }
    }

    public List<ReservationResponseDTO> findAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        return reservationMapper.toDtoLists(reservations);
    }

    public ReservationResponseDTO findReservationById(Long id) {
        Reservation reservation = reservationRepository.findReservationById(id);
        return reservationMapper.toDto(reservation);
    }

    public void deleteReservation(Long id) {
        reservationRepository.deleteById(id);
    }

    private ReservationResponseDTO convertToDTO(Reservation reservation) {
        return new ReservationResponseDTO(
                reservation.getId(),
                reservation.getDate(),
                new ReservationResponseDTO.ResidentSummaryDTO(
                        reservation.getResident().getId(),
                        reservation.getResident().getLogin()
                ),
                new ReservationResponseDTO.SpaceSummaryDTO(
                        reservation.getSpace().getId(),
                        reservation.getSpace().getType().toString()
                )
        );
    }


}
