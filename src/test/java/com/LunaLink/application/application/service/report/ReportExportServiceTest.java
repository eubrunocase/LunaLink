package com.LunaLink.application.application.service.report;

import com.LunaLink.application.application.ports.output.ReportExporterPort;
import com.LunaLink.application.application.ports.output.ReservationRepositoryPort;
import com.LunaLink.application.domain.enums.ReportExportStatus;
import com.LunaLink.application.domain.enums.ReportFormat;
import com.LunaLink.application.domain.enums.ReservationStatus;
import com.LunaLink.application.domain.enums.SpaceType;
import com.LunaLink.application.domain.enums.UserRoles;
import com.LunaLink.application.domain.model.reservation.Reservation;
import com.LunaLink.application.domain.model.space.Space;
import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.infrastructure.report.ReportExportJobStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReportExportServiceTest {

    @TempDir
    Path tempDir;

    private ReportExportJobStore jobStore;
    private ReportExporterPort reportExporter;
    private ReservationRepositoryPort reservationRepository;
    private PlatformTransactionManager transactionManager;
    private ReportExportService service;
    private ReportExporterPort.ReportExporterSession session;

    private final List<Reservation> allReservations = new ArrayList<>();

    @BeforeEach
    void setUp() throws IOException {
        jobStore = new ReportExportJobStore(tempDir);
        reportExporter = mock(ReportExporterPort.class);
        session = mock(ReportExporterPort.ReportExporterSession.class);
        reservationRepository = mock(ReservationRepositoryPort.class);
        transactionManager = mock(PlatformTransactionManager.class);
        when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));

        when(reportExporter.begin(any(), any())).thenReturn(session);

        service = new ReportExportService(jobStore, reportExporter, reservationRepository,
                transactionManager, 2, "Condomínio Teste");
    }

    private Reservation reservation(int day, SpaceType type) {
        Users user = new Users("Maria Silva", "102", "maria@test.com", "pass", UserRoles.RESIDENT_ROLE);
        Space space = new Space();
        space.setType(type);
        Reservation r = new Reservation();
        r.setId(UUID.randomUUID());
        r.setDate(LocalDate.of(2026, 5, day));
        r.setStatus(ReservationStatus.APPROVED);
        r.assignTo(user, space);
        return r;
    }

    @Test
    @DisplayName("Deve criar job com status PROCESSING e nome de arquivo correto")
    void createJob_shouldReturnProcessingJobWithFileName() {
        ReportExportJob job = service.createJob(5, 2026, ReportFormat.PDF);

        assertNotNull(job.getId());
        assertEquals(ReportExportStatus.PROCESSING, job.getStatus());
        assertEquals("relatorio-reservas-05-2026.pdf", job.getFileName());
        assertEquals("application/pdf", job.getContentType());
    }

    @Test
    @DisplayName("Deve rejeitar mês inválido na criação do job")
    void createJob_withInvalidMonth_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> service.createJob(13, 2026, ReportFormat.PDF));
        assertThrows(IllegalArgumentException.class, () -> service.createJob(0, 2026, ReportFormat.PDF));
    }

    @Test
    @DisplayName("Deve rejeitar ano inválido na criação do job")
    void createJob_withInvalidYear_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> service.createJob(5, 2019, ReportFormat.PDF));
    }

    @Test
    @DisplayName("Deve rejeitar formato nulo na criação do job")
    void createJob_withNullFormat_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> service.createJob(5, 2026, null));
    }

    @Test
    @DisplayName("Deve paginar por cursor até consumir todas as reservas e marcar READY")
    void generate_shouldReadAllPagesAndCompleteJob() throws Exception {
        List<Reservation> page1 = List.of(reservation(1, SpaceType.SALAO_FESTAS), reservation(2, SpaceType.CHURRASQUEIRA));
        List<Reservation> page2 = List.of(reservation(3, SpaceType.SALAO_FESTAS));
        allReservations.addAll(page1);
        allReservations.addAll(page2);

        when(reservationRepository.findReservationsForReportPage(
                eq(5), eq(2026), anyList(), anyList(), any(UUID.class), any()))
                .thenReturn(page1, page2);

        ReportExportJob job = service.createJob(5, 2026, ReportFormat.PDF);
        service.generate(job);

        assertEquals(ReportExportStatus.READY, job.getStatus());
        assertNotNull(job.getTempFile());
        assertTrue(Files.exists(job.getTempFile()));
        assertEquals(0L, job.getContentLength());
        verify(reservationRepository, times(2)).findReservationsForReportPage(
                eq(5), eq(2026), anyList(), anyList(), any(UUID.class), any());
        verify(session, times(2)).addRows(any());
        verify(session).finish();
    }

    @Test
    @DisplayName("Deve marcar o job como ERROR quando a geração falha")
    void generate_shouldMarkErrorOnFailure() throws IOException {
        when(reportExporter.begin(any(), any())).thenThrow(new IllegalStateException("falha simulada"));

        ReportExportJob job = service.createJob(5, 2026, ReportFormat.PDF);
        service.generate(job);

        assertEquals(ReportExportStatus.ERROR, job.getStatus());
        assertTrue(job.getErrorMessage().contains("falha simulada"));
    }

    @Test
    @DisplayName("getReadyJob deve lançar quando o job não está READY")
    void getReadyJob_whenNotReady_shouldThrow() {
        ReportExportJob job = service.createJob(5, 2026, ReportFormat.PDF);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.getReadyJob(job.getId()));
        assertTrue(ex.getMessage().contains("não está pronto"));
    }

    @Test
    @DisplayName("getReadyJob deve retornar o job pronto para download")
    void getReadyJob_whenReady_shouldReturnJob() throws IOException {
        Path file = tempDir.resolve("relatorio-reservas-05-2026.pdf");
        Files.write(file, new byte[]{0x25, 0x50, 0x44, 0x46});

        ReportExportJob job = service.createJob(5, 2026, ReportFormat.PDF).markCompleted();
        job.setTempFile(file);
        job.setContentLength(file.toFile().length());
        jobStore.update(job);

        ReportExportJob result = service.getReadyJob(job.getId());

        assertEquals(job.getId(), result.getId());
        assertEquals(ReportExportStatus.READY, result.getStatus());
    }

    @Test
    @DisplayName("getJob deve lançar quando o job não existe ou expirou")
    void getJob_whenMissing_shouldThrow() {
        assertThrows(IllegalStateException.class, () -> service.getJob("inexistente"));
    }
}
