package com.LunaLink.application.application.service.reservation;

import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.domain.enums.ReservationStatus;
import com.LunaLink.application.domain.events.ReservationApprovedEvent;
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
    public ReservationResponseDTO createReservation(ReservationRequestDTO data)  {
            Users r = userRepository.findById(data.userId())
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado. Verifique o ID e tente novamente."));
            Space s = spaceRepository.findSpaceById(data.spaceId())
                    .orElseThrow(() -> new IllegalArgumentException("Espaço não encontrado. Verifique o ID e tente novamente."));

            List<ReservationStatus> activeStatuses = List.of(ReservationStatus.PENDING, ReservationStatus.APPROVED);

            if (reservationRepository.existsByDateAndStatusIn(data.date(), activeStatuses)) {
                throw new IllegalStateException(
                        String.format("Data indisponível. Já existe uma reserva ativa (Pendente ou Aprovada) para o dia %s.",
                                data.date()));
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

            publisher.publishEvent(event);
            return reservationMapper.toDto(savedReservation);

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

        // Retorna falso se já houver qualquer reserva na data
        if (reservationRepository.existsByDateAndStatusIn(date, activeStatuses)) {
            System.out.println("Data indisponível: O condomínio já possui um evento reservado no dia " + date);
            return false;
        }
            return true;

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

        ReservationApprovedEvent event = new ReservationApprovedEvent(
                id,
                reservation.getUser().getId(),
                reservation.getDate(),
                reservation.getSpace()
        );
        publisher.publishEvent(event);

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
