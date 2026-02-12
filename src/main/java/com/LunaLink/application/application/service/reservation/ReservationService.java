package com.LunaLink.application.application.service.reservation;

import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.domain.model.reservation.Reservation;
import com.LunaLink.application.domain.model.space.Space;
import com.LunaLink.application.infrastructure.mapper.reservation.ReservationMapper;
import com.LunaLink.application.application.ports.input.ReservationServicePort;
import com.LunaLink.application.application.ports.output.ReservationRepositoryPort;
import com.LunaLink.application.infrastructure.repository.space.SpaceRepository;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationRequestDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationResponseDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReservationService implements ReservationServicePort {
    private final UserRepositoryPort userRepository;
    private final SpaceRepository spaceRepository;
    private final ReservationRepositoryPort reservationRepository;
    private final ReservationMapper reservationMapper;

    public ReservationService(UserRepositoryPort userRepository,
                              SpaceRepository spaceRepository,
                              ReservationRepositoryPort reservationRepository,
                              ReservationMapper reservationMapper) {
        this.userRepository = userRepository;
        this.spaceRepository = spaceRepository;
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
    }

    @Transactional
    @Override
    public ReservationResponseDTO createReservation(ReservationRequestDTO data) throws Exception {
        try {
            Users r = userRepository.findById(data.userId())
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
            if (reservationRepository.existsByUserAndDateAndSpace(r, data.date(), s)) {
                throw new IllegalStateException(
                        String.format("O residente '%s' já possui reserva para '%s' em %s.",
                                r.getLogin(), s.getType(), data.date()));
            }

            Reservation reservation = new Reservation();
            reservation.setDate(data.date());
            reservation.assignTo(r, s);
            Reservation savedReservation = reservationRepository.save(reservation);
            return reservationMapper.toDto(savedReservation);

        } catch (Exception e) {
            throw new Exception("Erro ao criar reserva: " + e.getMessage());
        }
    }

    @Override
    public List<ReservationResponseDTO> findAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        return reservationMapper.toDtoLists(reservations);
    }

    @Override
    public ReservationResponseDTO findReservationById(UUID id) {
        Optional<Reservation> reservation = reservationRepository.findById(id);
        ReservationResponseDTO reservationResponseDTO = reservationMapper.toDto(reservation.orElse(null));
        return reservationResponseDTO;
    }

    @Transactional
    @Override
    public void deleteReservation(UUID id) {
        try {
            Optional<Reservation> reservation = reservationRepository.findById(id);

        if (reservation == null) {
            throw new IllegalArgumentException("ERRO NO METODO DeleteReservation da classe de service, Reservation not found");
        }

            reservationRepository.deleteById(id);

        }  catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    @Override
    public ReservationResponseDTO updateReservation(UUID id, ReservationRequestDTO reservationRequestDTO) {
        try {
            Optional<Reservation> reservation = reservationRepository.findById(id);
            if (reservation == null) {
                throw new IllegalArgumentException("ERRO NO METODO UpdateReservation da classe de service, Reservation not found");
            }

            reservation.get().setDate(reservationRequestDTO.date());
            reservationRepository.save(reservation.get());

            return convertToDTO(reservation.get());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Boolean checkAvaliability (LocalDate date, Long spaceId, UUID user) {
        Users r = userRepository.findById(user).orElseThrow(()
                -> new IllegalArgumentException("ERRO NO METODO checkAvaliability da classe de service, Resident not found"));
        Space s = spaceRepository.findSpaceById(spaceId).orElseThrow(()
                -> new IllegalArgumentException("ERRO NO METODO checkAvaliability da classe de service, Space not found"));

        if (reservationRepository.existsByDate(date)) {
             System.out.println("Data indisponível: Já existe uma reserva para o dia " + date +
                     "." + "para o morador " + r.getLogin() + ".");
             return false;
         } else if (reservationRepository.existsByDateAndSpace(date, s)) {
             System.out.println("Data indisponível: Já existe uma reserva no espaço: " + s.getType() +
                     "." + "para o morador " + r.getLogin() + ".");
             return false;
         } else if (reservationRepository.existsByUserAndDateAndSpace(r, date, s)) {
             System.out.println("Data indisponível: Já existe uma reserva para o residente " + r.getLogin() +
                     "para " + s.getType() + " no dia " + date + ".");
             return false;
         } else {
             return true;
         }
    }

    private ReservationResponseDTO convertToDTO(Reservation reservation) {
        return new ReservationResponseDTO(
                reservation.getId(),
                reservation.getDate(),
                new ReservationResponseDTO.UserSummaryDTO(
                        reservation.getUser().getId(),
                        reservation.getUser().getLogin()
                ),
                new ReservationResponseDTO.SpaceSummaryDTO(
                        reservation.getSpace().getId(),
                        reservation.getSpace().getType().toString()
                )
        );
    }

}
