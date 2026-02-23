package com.LunaLink.application.application.service.reservation;

import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.domain.enums.ReservationStatus;
import com.LunaLink.application.domain.events.ReservationRequestedEvent;
import com.LunaLink.application.domain.model.space.Space;
import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.domain.model.reservation.Reservation;
import com.LunaLink.application.infrastructure.event.EventPublisher;
import com.LunaLink.application.infrastructure.mapper.reservation.ReservationMapper;
import com.LunaLink.application.application.ports.input.ReservationServicePort;
import com.LunaLink.application.application.ports.output.ReservationRepositoryPort;
import com.LunaLink.application.infrastructure.repository.space.SpaceRepository;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationRequestDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationResponseDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReservationService implements ReservationServicePort {
    private final UserRepositoryPort userRepository;
    private final SpaceRepository spaceRepository;
    private final ReservationRepositoryPort reservationRepository;
    private final ReservationMapper reservationMapper;
    private final EventPublisher publisher;

    public ReservationService(UserRepositoryPort userRepository,
                              SpaceRepository spaceRepository,
                              ReservationRepositoryPort reservationRepository,
                              ReservationMapper reservationMapper,
                              EventPublisher publisher) {
        this.userRepository = userRepository;
        this.spaceRepository = spaceRepository;
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
        this.publisher = publisher;
    }

    @Transactional
    @Override
    public ReservationResponseDTO createReservation(ReservationRequestDTO data) throws Exception {
        try {
            Users r = userRepository.findById(data.userId())
                    .orElseThrow(() -> new IllegalArgumentException("ERRO NO METODO CreateReservation da classe de service, Resident not found"));
            Space s = spaceRepository.findSpaceById(data.spaceId())
                    .orElseThrow(() -> new IllegalArgumentException("ERRO NO METODO CreateReservation da classe de service, space not found"));

            List<ReservationStatus> activeStatuses = List.of(ReservationStatus.PENDING, ReservationStatus.APPROVED);
            // Primeira checagem de conflito = Já existe uma reserva na mesma data com status de pendente ou aprovada?
//            if (reservationRepository.existsByDateAndStatus(data.date(), ReservationStatus.PENDING) ||
//                    reservationRepository.existsByDateAndStatus(data.date(), ReservationStatus.APPROVED)) {
//                throw new IllegalStateException(
//                        java.lang.String.format("Já existe uma reserva pendente ou aprovada para a data %s no espaço %s para o residente %s."
//                                , data.date(), s.getType(), r.getLogin()));
//            }
            // Segunda checagem de conflito = Outro usuário já reservou o MESMO espaço na MESMA data com status ativo?
            if (reservationRepository.existsByDateAndSpaceAndStatusIn(data.date(), s, activeStatuses)) {
                throw new IllegalStateException(
                        String.format("O espaço '%s' já possui uma reserva pendente ou aprovada em %s.",
                                s.getType(), data.date()));
            }
            // Terceira checagem de conflito = O mesmo usuário já tem reserva pro mesmo espaço e data?
            if (reservationRepository.existsByUserAndDateAndSpaceAndStatusIn(r, data.date(), s, activeStatuses)) {
                throw new IllegalStateException(
                        String.format("O residente '%s' já possui uma reserva ativa para '%s' em %s.",
                                r.getLogin(), s.getType(), data.date()));
            }

            Reservation reservation = new Reservation();
            reservation.setDate(data.date());
            reservation.setStatus(ReservationStatus.PENDING);
            reservation.setCreatedAt(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
            reservation.assignTo(r, s);
            Reservation savedReservation = reservationRepository.save(reservation);

            ReservationRequestedEvent event = new ReservationRequestedEvent(
                    savedReservation.getId(),
                    r.getId(),
                    savedReservation.getDate(),
                    s
            );

            publisher.publishReservationRequestedEvent(event);
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
            reservation.get().setStatus(ReservationStatus.CANCELLED);
            reservation.get().setCanceledAt(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
            reservationRepository.save(reservation.get());
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
        Users r = userRepository.findById(user).orElseThrow(() -> new IllegalArgumentException("ERRO: Resident not found"));
        Space s = spaceRepository.findSpaceById(spaceId).orElseThrow(() -> new IllegalArgumentException("ERRO: Space not found"));

        List<ReservationStatus> activeStatuses = List.of(ReservationStatus.PENDING, ReservationStatus.APPROVED);

        if (reservationRepository.existsByDateAndSpaceAndStatusIn(date, s, activeStatuses)) {
            System.out.println("Data indisponível: Já existe uma reserva no espaço: " + s.getType() + " na data " + date);
            return false;
        } else if (reservationRepository.existsByUserAndDateAndSpaceAndStatusIn(r, date, s, activeStatuses)) {
            System.out.println("Data indisponível: Já existe uma reserva para o residente " + r.getLogin() +
                    " para " + s.getType() + " no dia " + date + ".");
            return false;
        } else {
            return true;
        }
    }

    @Transactional
    @Override
    public ReservationResponseDTO approveReservation(UUID id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ERRO: Reserva não encontrada."));

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Apenas reservas com status PENDENTE podem ser aprovadas.");
        }

        reservation.setStatus(ReservationStatus.APPROVED);
        Reservation savedReservation = reservationRepository.save(reservation);

        // Disparar evento para informar ao morador que a reserva foi aprovada

        return convertToDTO(savedReservation);
    }

    @Transactional
    @Override
    public ReservationResponseDTO rejectReservation(UUID id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ERRO: Reserva não encontrada."));

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Apenas reservas com status PENDENTE podem ser rejeitadas.");
        }

        reservation.setStatus(ReservationStatus.REJECTED);
        Reservation savedReservation = reservationRepository.save(reservation);

        // Disparar evento para informar ao morador que a sua reserva foi rejeitada

        return convertToDTO(savedReservation);
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
                ),
                reservation.getStatus(),
                reservation.getCreatedAt(),
                reservation.getCanceledAt()
        );
    }

}
