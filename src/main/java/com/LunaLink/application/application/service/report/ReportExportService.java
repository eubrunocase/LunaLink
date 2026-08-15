package com.LunaLink.application.application.service.report;

import com.LunaLink.application.application.ports.output.ReportExporterPort;
import com.LunaLink.application.application.ports.output.ReservationRepositoryPort;
import com.LunaLink.application.domain.enums.ReportExportStatus;
import com.LunaLink.application.domain.enums.ReportFormat;
import com.LunaLink.application.domain.model.reservation.Reservation;
import com.LunaLink.application.infrastructure.report.ReportExportJobStore;
import com.LunaLink.application.web.dto.ReservationsDTO.MonthlyReservationReportDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ReportExportService {

    public static final int DEFAULT_PAGE_SIZE = 500;
    private static final UUID MIN_UUID = new UUID(0L, 0L);

    private final ReportExportJobStore jobStore;
    private final ReportExporterPort reportExporter;
    private final ReservationRepositoryPort reservationRepository;
    private final TransactionTemplate transactionTemplate;
    private final int pageSize;
    private final String condominiumName;

    @Autowired
    public ReportExportService(ReportExportJobStore jobStore,
                               ReportExporterPort reportExporter,
                               ReservationRepositoryPort reservationRepository,
                               PlatformTransactionManager transactionManager,
                               @Value("${app.report.condominium-name:LunaLink}") String condominiumName) {
        this(jobStore, reportExporter, reservationRepository, transactionManager, DEFAULT_PAGE_SIZE, condominiumName);
    }

    ReportExportService(ReportExportJobStore jobStore,
                        ReportExporterPort reportExporter,
                        ReservationRepositoryPort reservationRepository,
                        PlatformTransactionManager transactionManager,
                        int pageSize,
                        String condominiumName) {
        this.jobStore = jobStore;
        this.reportExporter = reportExporter;
        this.reservationRepository = reservationRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setReadOnly(true);
        this.pageSize = pageSize;
        this.condominiumName = condominiumName;
    }

    public ReportExportJob createJob(int month, int year, ReportFormat format) {
        validateMonth(month);
        validateYear(year);
        if (format == null) {
            throw new IllegalArgumentException("Formato inválido: informe PDF ou DOCX.");
        }
        return jobStore.create(ReportExportJob.create(month, year, format));
    }

    @Async("taskExecutor")
    public void generate(ReportExportJob job) {
        try {
            transactionTemplate.executeWithoutResult(status -> generateFile(job));
            jobStore.update(job.markCompleted());
        } catch (Exception e) {
            jobStore.update(job.markFailed(e.getMessage()));
        }
    }

    private void generateFile(ReportExportJob job) {
        Path tempFile = jobStore.getTempDir().resolve(job.getFileName());
        ReportContext context = new ReportContext(job.getMonth(), job.getYear(), job.getFormat(),
                condominiumName, Instant.now());

        try (OutputStream out = Files.newOutputStream(tempFile)) {
            ReportExporterPort.ReportExporterSession session = reportExporter.begin(context, out);

            UUID afterId = MIN_UUID;
            int fetched;
            do {
                List<Reservation> page = reservationRepository.findReservationsForReportPage(
                        job.getMonth(), job.getYear(),
                        ReportFilters.VALID_STATUSES, ReportFilters.BILLABLE_SPACE_TYPES,
                        afterId, PageRequest.of(0, pageSize));
                List<MonthlyReservationReportDTO> rows = page.stream().map(this::toReportDTO).toList();
                session.addRows(rows);
                fetched = page.size();
                if (fetched > 0) {
                    afterId = page.get(fetched - 1).getId();
                }
            } while (fetched == pageSize);

            session.finish();
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gerar o arquivo do relatório.", e);
        }

        job.setTempFile(tempFile);
        job.setContentLength(tempFile.toFile().length());
    }

    private MonthlyReservationReportDTO toReportDTO(Reservation reservation) {
        return new MonthlyReservationReportDTO(
                reservation.getUser().getName(),
                reservation.getUser().getApartment(),
                reservation.getDate(),
                reservation.getSpace().getType().name()
        );
    }

    public ReportExportJob getJob(String jobId) {
        return jobStore.get(jobId)
                .orElseThrow(() -> new IllegalStateException("Job de exportação não encontrado ou expirado."));
    }

    public ReportExportJob getReadyJob(String jobId) {
        ReportExportJob job = getJob(jobId);
        if (job.getStatus() != ReportExportStatus.READY) {
            throw new IllegalStateException("O relatório ainda não está pronto para download.");
        }
        return job;
    }

    private void validateMonth(int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Mês inválido: " + month);
        }
    }

    private void validateYear(int year) {
        if (year < 2020) {
            throw new IllegalArgumentException("Ano inválido: " + year);
        }
    }
}
