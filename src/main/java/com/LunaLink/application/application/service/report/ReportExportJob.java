package com.LunaLink.application.application.service.report;

import com.LunaLink.application.domain.enums.ReportExportStatus;
import com.LunaLink.application.domain.enums.ReportFormat;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

public class ReportExportJob {

    private final String id;
    private final int month;
    private final int year;
    private final ReportFormat format;
    private final String fileName;
    private final String contentType;
    private final Instant createdAt;

    private ReportExportStatus status;
    private Path tempFile;
    private long contentLength;
    private String errorMessage;
    private Instant completedAt;

    private ReportExportJob(String id, int month, int year, ReportFormat format, ReportExportStatus status,
                            String fileName, String contentType, Path tempFile, long contentLength,
                            String errorMessage, Instant createdAt, Instant completedAt) {
        this.id = id;
        this.month = month;
        this.year = year;
        this.format = format;
        this.status = status;
        this.fileName = fileName;
        this.contentType = contentType;
        this.tempFile = tempFile;
        this.contentLength = contentLength;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public static ReportExportJob create(int month, int year, ReportFormat format) {
        String id = UUID.randomUUID().toString();
        String fileName = "relatorio-reservas-%02d-%d.%s".formatted(month, year, format.extension());
        return new ReportExportJob(id, month, year, format, ReportExportStatus.PROCESSING,
                fileName, format.contentType(), null, 0L, null, Instant.now(), null);
    }

    public ReportExportJob markCompleted() {
        this.status = ReportExportStatus.READY;
        this.completedAt = Instant.now();
        return this;
    }

    public ReportExportJob markFailed(String message) {
        this.status = ReportExportStatus.ERROR;
        this.errorMessage = message;
        this.completedAt = Instant.now();
        return this;
    }

    public String getId() {
        return id;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public ReportFormat getFormat() {
        return format;
    }

    public ReportExportStatus getStatus() {
        return status;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public Path getTempFile() {
        return tempFile;
    }

    public void setTempFile(Path tempFile) {
        this.tempFile = tempFile;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
