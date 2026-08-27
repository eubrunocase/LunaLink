package com.LunaLink.application.application.service.reservation;

import com.LunaLink.application.application.ports.output.ReservationRepositoryPort;
import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.application.service.report.ReportExportJob;
import com.LunaLink.application.application.service.report.ReportExportService;
import com.LunaLink.application.domain.enums.ReportExportStatus;
import com.LunaLink.application.domain.enums.ReportFormat;
import com.LunaLink.application.domain.enums.ReservationStatus;
import com.LunaLink.application.domain.enums.SpaceType;
import com.LunaLink.application.domain.enums.UserRoles;
import com.LunaLink.application.domain.events.reservationEvents.ReservationApprovedEvent;
import com.LunaLink.application.domain.events.reservationEvents.ReservationAwaitingInspectionEvent;
import com.LunaLink.application.domain.events.reservationEvents.ReservationRejectedEvent;
import com.LunaLink.application.domain.events.reservationEvents.ReservationRequestedEvent;
import com.LunaLink.application.domain.model.reservation.Reservation;
import com.LunaLink.application.domain.model.space.Space;
import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.infrastructure.eventPublisher.EventPublisher;
import com.LunaLink.application.infrastructure.mapper.reservation.ReservationMapper;
import com.LunaLink.application.infrastructure.repository.space.SpaceRepository;
import com.LunaLink.application.web.dto.ReservationsDTO.MonthlyReservationReportDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReportExportJobResponseDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationRequestDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private SpaceRepository spaceRepository;
    @Mock
    private ReservationRepositoryPort reservationRepository;
    @Mock
    private ReservationMapper reservationMapper;
    @Mock
    private EventPublisher publisher;
    @Mock
    private ReportExportService reportExportService;

    @InjectMocks
    private ReservationService service;

    private Users user;
    private Space space;
    private Reservation reservation;
    private ReservationRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        user = new Users("User", "101", "testUser@email.com", "pass", UserRoles.RESIDENT_ROLE);
        user.setId(UUID.randomUUID());

        space = new Space();
        space.setId(1L);
        space.setType(SpaceType.SALAO_FESTAS);

        reservation = new Reservation();
        reservation.setId(UUID.randomUUID());
        reservation.setDate(LocalDate.now().plusDays(1));
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setUser(user);
        reservation.setSpace(space);

        requestDTO = new ReservationRequestDTO(user.getId(), LocalDate.now().plusDays(1), space.getId(), null, null);
    }

    @Test
    @DisplayName("Deve criar reserva com sucesso quando data disponível")
    void createReservation_ShouldSucceed_WhenDateIsAvailable() {
        // Arrange
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(spaceRepository.findSpaceById(space.getId())).thenReturn(Optional.of(space));
        when(reservationRepository.findActiveByDateAndSpaceTypes(any(), anyList(), anyList())).thenReturn(List.of());
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
        
        ReservationResponseDTO expectedResponse = new ReservationResponseDTO(
                reservation.getId(), reservation.getDate(), null, null, 
                ReservationStatus.PENDING, null, null, null, LocalDateTime.now(), null
        );
        when(reservationMapper.toDto(any(Reservation.class))).thenReturn(expectedResponse);

        // Act
        ReservationResponseDTO result = service.createReservation(requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(ReservationStatus.PENDING, result.status());
        verify(publisher, times(1)).publishEvent(any(ReservationRequestedEvent.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar reserva em data indisponível")
    void createReservation_ShouldThrowException_WhenDateUnavailable() {
        // Arrange
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(spaceRepository.findSpaceById(space.getId())).thenReturn(Optional.of(space));

        Users otherUser = new Users("Other", "202", "other@test.com", "pass", UserRoles.RESIDENT_ROLE);
        otherUser.setId(UUID.randomUUID());

        Space salaoSpace = new Space();
        salaoSpace.setId(10L);
        salaoSpace.setType(SpaceType.SALAO_FESTAS);

        Reservation existingReservation = new Reservation();
        existingReservation.setId(UUID.randomUUID());
        existingReservation.setDate(LocalDate.now().plusDays(1));
        existingReservation.setStatus(ReservationStatus.CONFIRMED);
        existingReservation.setUser(otherUser);
        existingReservation.setSpace(salaoSpace);

        when(reservationRepository.findActiveByDateAndSpaceTypes(any(), anyList(), anyList()))
                .thenReturn(List.of(existingReservation));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> service.createReservation(requestDTO));
            
        assertTrue(exception.getMessage().contains("Data indisponível"));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve aprovar reserva de Campo de Futebol e confirmar direto (sem vistoria)")
    void approveReservation_ShouldConfirmDirectly_WhenCampoFutebol() {
        // Arrange
        Space campoFutebol = new Space();
        campoFutebol.setId(2L);
        campoFutebol.setType(SpaceType.CAMPO_FUTEBOL);

        Reservation reservationCampo = new Reservation();
        reservationCampo.setId(UUID.randomUUID());
        reservationCampo.setDate(LocalDate.now().plusDays(1));
        reservationCampo.setStatus(ReservationStatus.PENDING);
        reservationCampo.setUser(user);
        reservationCampo.setSpace(campoFutebol);

        when(reservationRepository.findById(reservationCampo.getId())).thenReturn(Optional.of(reservationCampo));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservationCampo);

        // Act
        ReservationResponseDTO result = service.approveReservation(reservationCampo.getId());

        // Assert
        assertEquals(ReservationStatus.CONFIRMED, reservationCampo.getStatus());
        verify(publisher, times(1)).publishEvent(any(ReservationApprovedEvent.class));
    }

    @Test
    @DisplayName("Deve aprovar reserva de Salão de Festas e aguardar vistoria")
    void approveReservation_ShouldAwaitInspection_WhenSalaoFestas() {
        // Arrange
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        // Act
        ReservationResponseDTO result = service.approveReservation(reservation.getId());

        // Assert
        assertEquals(ReservationStatus.AWAITING_INSPECTION, reservation.getStatus());
        verify(publisher, times(1)).publishEvent(any(ReservationApprovedEvent.class));
        verify(publisher, times(1)).publishEvent(any(ReservationAwaitingInspectionEvent.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar aprovar reserva não pendente")
    void approveReservation_ShouldThrowException_WhenNotPending() {
        // Arrange
        reservation.setStatus(ReservationStatus.REJECTED);
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> service.approveReservation(reservation.getId()));
            
        assertEquals("Apenas reservas com status PENDENTE podem ser aprovadas.", exception.getMessage());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve rejeitar reserva pendente com sucesso")
    void rejectReservation_ShouldUpdateStatus_WhenPending() {
        // Arrange
        UUID reservationId = reservation.getId();
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        // Act
        service.rejectReservation(reservationId);

        // Assert
        assertEquals(ReservationStatus.REJECTED, reservation.getStatus());
        verify(publisher, times(1)).publishEvent(any(ReservationRejectedEvent.class));
    }
    
    @Test
    @DisplayName("Deve verificar disponibilidade corretamente")
    void checkAvailability_ShouldReturnTrue_WhenAvailable() {
        // Arrange
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(spaceRepository.findSpaceById(space.getId())).thenReturn(Optional.of(space));
        when(reservationRepository.findActiveByDateAndSpaceTypes(any(), anyList(), anyList())).thenReturn(List.of());

        // Act
        Boolean isAvailable = service.checkAvaliability(LocalDate.now(), space.getId(), user.getId());

        // Assert
        assertTrue(isAvailable);
    }

    @Test
    @DisplayName("Deve gerar relatório mensal corretamente")
    void generateMonthlyReport_ShouldReturnList_WhenFound() {
        // Arrange
        int month = 5;
        int year = 2026;
        when(reservationRepository.findReservationsForReport(eq(month), eq(year), anyList(), anyList()))
                .thenReturn(List.of(reservation));

        // Act
        List<MonthlyReservationReportDTO> report = service.generateMonthlyReport(month, year);

        // Assert
        assertNotNull(report);
        assertEquals(1, report.size());
        assertEquals("User", report.get(0).residentName());
        assertEquals("101", report.get(0).apartment());
        assertEquals("SALAO_FESTAS", report.get(0).spaceType());
    }

    @Test
    @DisplayName("Deve lançar exceção ao gerar relatório com mês inválido")
    void generateMonthlyReport_ShouldThrowException_WhenInvalidMonth() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.generateMonthlyReport(0, 2026));
        assertTrue(exception.getMessage().contains("Mês inválido"));
        verify(reservationRepository, never()).findReservationsForReport(anyInt(), anyInt(), anyList(), anyList());
    }

    @Test
    @DisplayName("Deve lançar exceção ao gerar relatório com ano inválido")
    void generateMonthlyReport_ShouldThrowException_WhenInvalidYear() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.generateMonthlyReport(5, 2019));
        assertTrue(exception.getMessage().contains("Ano inválido"));
        verify(reservationRepository, never()).findReservationsForReport(anyInt(), anyInt(), anyList(), anyList());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver reservas no período")
    void generateMonthlyReport_ShouldReturnEmptyList_WhenNoReservations() {
        when(reservationRepository.findReservationsForReport(eq(5), eq(2026), anyList(), anyList()))
                .thenReturn(List.of());

        List<MonthlyReservationReportDTO> report = service.generateMonthlyReport(5, 2026);

        assertNotNull(report);
        assertTrue(report.isEmpty());
    }

    @Test
    @DisplayName("Deve filtrar relatório por status confirmado e espaços tarifados")
    void generateMonthlyReport_ShouldFilterByConfirmedStatusAndBillableSpaces() {
        service.generateMonthlyReport(5, 2026);

        verify(reservationRepository).findReservationsForReport(
                eq(5),
                eq(2026),
                eq(List.of(ReservationStatus.CONFIRMED)),
                eq(List.of(SpaceType.SALAO_FESTAS, SpaceType.CHURRASQUEIRA))
        );
    }

    @Test
    @DisplayName("Deve criar job de exportação e disparar geração assíncrona")
    void createMonthlyReportExport_ShouldCreateJobAndTriggerGeneration() {
        ReportExportJob job = ReportExportJob.create(5, 2026, ReportFormat.PDF);
        when(reportExportService.createJob(5, 2026, ReportFormat.PDF)).thenReturn(job);

        ReportExportJobResponseDTO response = service.createMonthlyReportExport(5, 2026, ReportFormat.PDF);

        assertNotNull(response);
        assertEquals(job.getId(), response.jobId());
        assertEquals(ReportExportStatus.PROCESSING, response.status());
        verify(reportExportService).generate(job);
    }

    @Test
    @DisplayName("Deve consultar o status de um job de exportação")
    void getMonthlyReportExportStatus_ShouldReturnJobStatus() {
        ReportExportJob job = ReportExportJob.create(5, 2026, ReportFormat.DOCX).markCompleted();
        when(reportExportService.getJob(job.getId())).thenReturn(job);

        ReportExportJobResponseDTO response = service.getMonthlyReportExportStatus(job.getId());

        assertEquals(job.getId(), response.jobId());
        assertEquals(ReportExportStatus.READY, response.status());
    }

    @Test
    @DisplayName("Deve retornar o job pronto para download")
    void getMonthlyReportExportFile_ShouldReturnReadyJob() {
        ReportExportJob job = ReportExportJob.create(5, 2026, ReportFormat.PDF).markCompleted();
        job.setTempFile(Path.of("/tmp/relatorio.pdf"));
        when(reportExportService.getReadyJob(job.getId())).thenReturn(job);

        ReportExportJob result = service.getMonthlyReportExportFile(job.getId());

        assertEquals(job, result);
    }
}
