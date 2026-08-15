package com.LunaLink.application.infrastructure.report;

import com.LunaLink.application.application.ports.output.ReportExporterPort;
import com.LunaLink.application.application.service.report.ReportContext;
import com.LunaLink.application.domain.enums.ReportFormat;
import com.LunaLink.application.web.dto.ReservationsDTO.MonthlyReservationReportDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ReportExporterTest {

    private final TemplateEngine templateEngine = createTemplateEngine();

    private static TemplateEngine createTemplateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }

    private ReportContext context() {
        return new ReportContext(5, 2026, ReportFormat.PDF, "Condomínio Teste", Instant.parse("2026-05-01T10:00:00Z"));
    }

    private List<MonthlyReservationReportDTO> rows() {
        return List.of(
                new MonthlyReservationReportDTO("Maria Silva", "102", LocalDate.of(2026, 5, 15), "CHURRASQUEIRA"),
                new MonthlyReservationReportDTO("João Souza", "203", LocalDate.of(2026, 5, 20), "SALAO_FESTAS")
        );
    }

    @Test
    @DisplayName("Deve gerar um DOCX válido (magic bytes PK) com as linhas do relatório")
    void docx_shouldProduceValidPackage() throws IOException {
        ReportExporter exporter = new ReportExporter(templateEngine);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ReportExporterPort.ReportExporterSession session = exporter.begin(
                new ReportContext(5, 2026, ReportFormat.DOCX, "Condomínio Teste", Instant.now()), out);

        session.addRows(rows());
        session.finish();

        byte[] bytes = out.toByteArray();
        assertTrue(bytes.length > 0, "O DOCX gerado não pode ser vazio");
        assertTrue(bytes[0] == 'P' && bytes[1] == 'K', "Esperado magic bytes PK (ZIP/DOCX), foi: "
                + Integer.toHexString(bytes[0] & 0xff));
    }

    @Test
    @DisplayName("Deve gerar um PDF válido (magic bytes %PDF) com as linhas do relatório")
    void pdf_shouldProduceValidPdf() throws IOException {
        boolean fontAvailable;
        try {
            fontAvailable = ReportExporter.resolveFontFile() != null;
        } catch (IllegalStateException e) {
            fontAvailable = false;
        }
        assumeTrue(fontAvailable, "Nenhuma fonte TTF disponível para gerar PDF");

        ReportExporter exporter = new ReportExporter(templateEngine);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ReportExporterPort.ReportExporterSession session = exporter.begin(context(), out);

        session.addRows(rows());
        session.finish();

        byte[] bytes = out.toByteArray();
        assertTrue(bytes.length > 0, "O PDF gerado não pode ser vazio");
        assertEquals('%', (char) bytes[0]);
        assertEquals('P', (char) bytes[1]);
        assertEquals('D', (char) bytes[2]);
        assertEquals('F', (char) bytes[3]);
    }

    @Test
    @DisplayName("DOCX sem reservas deve conter a mensagem de período vazio")
    void docx_withNoRows_shouldAddEmptyMessage() throws IOException {
        ReportExporter exporter = new ReportExporter(templateEngine);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ReportExporterPort.ReportExporterSession session = exporter.begin(
                new ReportContext(5, 2026, ReportFormat.DOCX, "Condomínio Teste", Instant.now()), out);

        session.addRows(List.of());
        session.finish();

        String documentXml = readDocxEntry(out.toByteArray(), "word/document.xml");
        assertTrue(documentXml.contains("Nenhuma reserva no período"),
                "Esperado texto de período vazio no DOCX");
    }

    private String readDocxEntry(byte[] docxBytes, String entryName) throws IOException {
        try (java.util.zip.ZipInputStream zip = new java.util.zip.ZipInputStream(
                new java.io.ByteArrayInputStream(docxBytes))) {
            java.util.zip.ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.getName().equals(entryName)) {
                    return new String(zip.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                }
            }
        }
        throw new IllegalStateException("Entrada " + entryName + " não encontrada no DOCX");
    }
}
