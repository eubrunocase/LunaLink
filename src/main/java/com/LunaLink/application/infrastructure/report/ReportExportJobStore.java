package com.LunaLink.application.infrastructure.report;

import com.LunaLink.application.application.service.report.ReportExportJob;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ReportExportJobStore {

    public static final Duration REPORT_TTL = Duration.ofMinutes(30);

    private final ConcurrentHashMap<String, ReportExportJob> jobs = new ConcurrentHashMap<>();
    private final Path tempDir;

    public ReportExportJobStore() {
        this(Paths.get(System.getProperty("java.io.tmpdir"), "lunalink-reports"));
    }

    public ReportExportJobStore(Path tempDir) {
        this.tempDir = tempDir;
        try {
            Files.createDirectories(tempDir);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível criar o diretório temporário de relatórios: " + tempDir, e);
        }
    }

    public ReportExportJob create(ReportExportJob job) {
        jobs.put(job.getId(), job);
        return job;
    }

    public Optional<ReportExportJob> get(String jobId) {
        return Optional.ofNullable(jobs.get(jobId));
    }

    public ReportExportJob update(ReportExportJob job) {
        jobs.put(job.getId(), job);
        return job;
    }

    public Path getTempDir() {
        return tempDir;
    }

    @Scheduled(fixedDelay = 60_000)
    public void cleanupExpired() {
        Instant cutoff = Instant.now().minus(REPORT_TTL);
        jobs.values().removeIf(job -> {
            boolean expired = job.getCreatedAt().isBefore(cutoff);
            if (expired) {
                deleteTempFile(job);
            }
            return expired;
        });
    }

    private void deleteTempFile(ReportExportJob job) {
        Path file = job.getTempFile();
        if (file != null) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException ignored) {
                // melhor esforço — o TTL do SO eventualmente limpa o arquivo
            }
        }
    }
}
