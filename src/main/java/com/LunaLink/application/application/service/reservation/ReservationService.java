package com.LunaLink.application.application.service.reservation;

import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.application.service.report.ReportExportJob;
import com.LunaLink.application.application.service.report.ReportExportService;
import com.LunaLink.application.application.service.report.ReportFilters;
import com.LunaLink.application.domain.enums.ReportFormat;
import com.LunaLink.application.domain.enums.ReservationStatus;
import com.LunaLink.application.domain.enums.SpaceType;
import com.LunaLink.application.domain.events.reservationEvents.ReservationApprovedEvent;
import com.LunaLink.application.domain.events.reservationEvents.ReservationAwaitingInspectionEvent;
import com.LunaLink.application.domain.events.reservationEvents.ReservationRejectedEvent;
import com.LunaLink.application.domain.events.reservationEvents.ReservationRequestedEvent;
import com.LunaLink.application.domain.model.space.Space;
import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.domain.model.reservation.Reservation;
import com.LunaLink.application.infrastructure.config.SpaceEquipmentCatalog;
import com.LunaLink.application.infrastructure.eventPublisher.EventPublisher;
import com.LunaLink.application.infrastructure.mapper.reservation.ReservationMapper;
import com.LunaLink.application.application.ports.input.ReservationServicePort;
import com.LunaLink.application.application.ports.output.ReservationRepositoryPort;
import com.LunaLink.application.domain.enums.InspectionType;
import com.LunaLink.application.web.dto.ReservationsDTO.InspectionPendingReservationDTO;
import com.LunaLink.application.infrastructure.repository.space.SpaceRepository;
import com.LunaLink.application.web.dto.ReservationsDTO.MonthlyReservationReportDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReportExportJobResponseDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationRequestDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationResponseDTO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReservationService implements ReservationServicePort {
    private final UserRepositoryPort userRepository;
    private final SpaceRepository spaceRepository;
    private final ReservationRepositoryPort reservationRepository;
    private final ReservationMapper reservationMapper;
    private final EventPublisher publisher;
    private final ReportExportService reportExportService;

    public ReservationService(UserRepositoryPort userRepository,
                              SpaceRepository spaceRepository,
                              ReservationRepositoryPort reservationRepository,
                              ReservationMapper reservationMapper,
                              EventPublisher publisher,
                              ReportExportService reportExportService) {
        this.userRepository = userRepository;
        this.spaceRepository = spaceRepository;
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
        this.publisher = publisher;
        this.reportExportService = reportExportService;
    }

    private static final List<SpaceType> EXCLUSIVE_SPACE_TYPES = List.of(
            SpaceType.SALAO_FESTAS,
            SpaceType.CHURRASQUEIRA,
            SpaceType.CAMPO_FUTEBOL
    );

    private static final List<ReservationStatus> ACTIVE_STATUSES = List.of(
            ReservationStatus.PENDING,
            ReservationStatus.AWAITING_INSPECTION,
            ReservationStatus.AWAITING_SIGNATURE,
            ReservationStatus.CONFIRMED
    );

    @Transactional
    @Override
    public ReservationResponseDTO createReservation(ReservationRequestDTO data)  {
            Users r = userRepository.findById(data.userId())
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado. Verifique o ID e tente novamente."));
            Space s = spaceRepository.findSpaceById(data.spaceId())
                    .orElseThrow(() -> new IllegalArgumentException("Espaço não encontrado. Verifique o ID e tente novamente."));

            validateDailyExclusivity(data.date(), r.getId(), s.getType());

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

    private void validateDailyExclusivity(LocalDate date, UUID requestingUserId, SpaceType requestedSpaceType) {
        List<Reservation> activeReservations = reservationRepository
                .findActiveByDateAndSpaceTypes(date, EXCLUSIVE_SPACE_TYPES, ACTIVE_STATUSES);

        if (activeReservations.isEmpty()) {
            return;
        }

        boolean sameUserHasAllReservations = activeReservations.stream()
                .allMatch(r -> r.getUser().getId().equals(requestingUserId));

        if (sameUserHasAllReservations) {
            return;
        }

        boolean hasDifferentUserReservation = activeReservations.stream()
                .anyMatch(r -> !r.getUser().getId().equals(requestingUserId));

        if (hasDifferentUserReservation) {
            throw new IllegalStateException(
                    String.format("Data indisponível. Já existe uma reserva ativa para o dia %s em um dos espaços exclusivos (Salão de Festas, Churrasqueira ou Campo de Futebol).", date));
        }
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> findReservationByUserId(UUID userId) {
        List<Reservation> reservations = reservationRepository.findByUserIdWithUserAndSpace(userId);
        return reservationMapper.toDtoLists(reservations);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ReservationResponseDTO> findReservationsByUserId(UUID userId) {
        List<Reservation> reservations = reservationRepository.findByUserIdWithUserAndSpace(userId);
        return reservationMapper.toDtoLists(reservations);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ReservationResponseDTO> findAllReservations() {
        List<Reservation> reservations = reservationRepository.findAllWithUserAndSpace();
        return reservationMapper.toDtoLists(reservations);
    }

    @Transactional(readOnly = true)
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

        List<Reservation> activeReservations = reservationRepository
                .findActiveByDateAndSpaceTypes(date, EXCLUSIVE_SPACE_TYPES, ACTIVE_STATUSES);

        if (activeReservations.isEmpty()) {
            return true;
        }

        boolean sameUserHasAllReservations = activeReservations.stream()
                .allMatch(res -> res.getUser().getId().equals(user));

        return sameUserHasAllReservations;
    }

    @Transactional
    @Override
    public ReservationResponseDTO approveReservation(UUID id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ERRO: Reserva não encontrada."));

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Apenas reservas com status PENDENTE podem ser aprovadas.");
        }

        com.LunaLink.application.domain.enums.SpaceType spaceType = reservation.getSpace().getType();
        boolean requiresInspection = com.LunaLink.application.infrastructure.config.SpaceEquipmentCatalog.requiresInspection(spaceType);

        if (requiresInspection) {
            reservation.setStatus(ReservationStatus.AWAITING_INSPECTION);
        } else {
            reservation.setStatus(ReservationStatus.CONFIRMED);
        }

        Reservation savedReservation = reservationRepository.save(reservation);

        ReservationApprovedEvent approvedEvent = new ReservationApprovedEvent(
                id,
                reservation.getUser().getId(),
                reservation.getDate(),
                reservation.getSpace()
        );
        publisher.publishEvent(approvedEvent);

        if (requiresInspection) {
            ReservationAwaitingInspectionEvent inspectionEvent = new ReservationAwaitingInspectionEvent(
                    id,
                    reservation.getUser().getId(),
                    reservation.getDate(),
                    reservation.getSpace()
            );
            publisher.publishEvent(inspectionEvent);
        }

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

        ReservationRejectedEvent event = new ReservationRejectedEvent(
                id,
                reservation.getUser().getId(),
                reservation.getDate(),
                reservation.getSpace()
        );
        publisher.publishEvent(event);
        return convertToDTO(savedReservation);
    }

    @Transactional(readOnly = true)
    @Override
    public List<MonthlyReservationReportDTO> generateMonthlyReport(int month, int year) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Mês inválido: " + month);
        }
        if (year < 2020) {
            throw new IllegalArgumentException("Ano inválido: " + year);
        }

        List<Reservation> reservations = reservationRepository.findReservationsForReport(
                month, year, ReportFilters.VALID_STATUSES, ReportFilters.BILLABLE_SPACE_TYPES
        );

        return reservations.stream()
                .map(this::convertToReportDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ReportExportJobResponseDTO createMonthlyReportExport(int month, int year, ReportFormat format) {
        ReportExportJob job = reportExportService.createJob(month, year, format);
        reportExportService.generate(job);
        return ReportExportJobResponseDTO.from(job);
    }

    @Override
    public ReportExportJobResponseDTO getMonthlyReportExportStatus(String jobId) {
        return ReportExportJobResponseDTO.from(reportExportService.getJob(jobId));
    }

    @Override
    public ReportExportJob getMonthlyReportExportFile(String jobId) {
        return reportExportService.getReadyJob(jobId);
    }

    @Override
    public List<InspectionPendingReservationDTO> findPendingInspectionReservations() {
        List<SpaceType> inspectionSpaces = List.of(SpaceType.SALAO_FESTAS, SpaceType.CHURRASQUEIRA);

        List<Reservation> preEventReservations = reservationRepository
            .findByDateAndStatusInAndSpaceTypes(
                LocalDate.now(),
                List.of(ReservationStatus.AWAITING_INSPECTION),
                inspectionSpaces
            );

        List<Reservation> postEventReservations = reservationRepository
            .findByDateAndStatusInAndSpaceTypes(
                LocalDate.now().minusDays(1),
                List.of(ReservationStatus.CONFIRMED),
                inspectionSpaces
            );

        List<InspectionPendingReservationDTO> result = new ArrayList<>();

        preEventReservations.forEach(r -> result.add(new InspectionPendingReservationDTO(
            r.getId(),
            r.getDate(),
            r.getSpace().getType(),
            r.getSpace().getType().name(),
            InspectionType.PRE_EVENT,
            r.getUser().getName()
        )));

        postEventReservations.forEach(r -> result.add(new InspectionPendingReservationDTO(
            r.getId(),
            r.getDate(),
            r.getSpace().getType(),
            r.getSpace().getType().name(),
            InspectionType.POST_EVENT,
            r.getUser().getName()
        )));

        return result;
    }

    private MonthlyReservationReportDTO convertToReportDTO(Reservation reservation) {
        return new MonthlyReservationReportDTO(
                reservation.getUser().getName(),
                reservation.getUser().getApartment(),
                reservation.getDate(),
                reservation.getSpace().getType().toString()
        );
    }

    private ReservationResponseDTO convertToDTO(Reservation reservation) {
        return reservationMapper.toDto(reservation);
    }

}
