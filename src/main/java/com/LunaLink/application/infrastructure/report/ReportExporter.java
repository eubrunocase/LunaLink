package com.LunaLink.application.infrastructure.report;

import com.LunaLink.application.application.ports.output.ReportExporterPort;
import com.LunaLink.application.application.service.report.ReportContext;
import com.LunaLink.application.web.dto.ReservationsDTO.MonthlyReservationReportDTO;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
public class ReportExporter implements ReportExporterPort {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String PDF_FONT_FAMILY = "ReportFont";
    private static final Map<String, String> SPACE_LABELS = Map.of(
            "SALAO_FESTAS", "Salão de Festas",
            "CHURRASQUEIRA", "Churrasqueira",
            "ACADEMIA", "Academia",
            "CAMPO_FUTEBOL", "Campo de Futebol"
    );

    private final TemplateEngine templateEngine;

    public ReportExporter(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public ReportExporterSession begin(ReportContext context, OutputStream output) {
        return switch (context.format()) {
            case PDF -> new PdfSession(context, output, templateEngine);
            case DOCX -> new DocxSession(context, output);
        };
    }

    private static final class DocxSession implements ReportExporterSession {
        private final XWPFDocument document = new XWPFDocument();
        private final XWPFTable table;
        private final OutputStream output;
        private int rowCount;

        DocxSession(ReportContext context, OutputStream output) {
            this.output = output;

            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setText(context.title());
            titleRun.setBold(true);
            titleRun.setFontSize(16);

            XWPFParagraph subtitle = document.createParagraph();
            subtitle.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun subtitleRun = subtitle.createRun();
            subtitleRun.setText(context.subtitle());
            subtitleRun.setFontSize(10);
            subtitleRun.setColor("777777");

            table = document.createTable(1, 4);
            XWPFTableRow header = table.getRow(0);
            setHeaderCell(header, 0, "Morador");
            setHeaderCell(header, 1, "Apartamento");
            setHeaderCell(header, 2, "Data");
            setHeaderCell(header, 3, "Espaço");
        }

        private void setHeaderCell(XWPFTableRow row, int index, String text) {
            XWPFTableCell cell = row.getCell(index);
            cell.setText(text);
            XWPFRun run = cell.getParagraphs().get(0).getRuns().get(0);
            run.setBold(true);
        }

        @Override
        public void addRows(List<MonthlyReservationReportDTO> rows) {
            for (MonthlyReservationReportDTO row : rows) {
                XWPFTableRow tableRow = table.createRow();
                tableRow.getCell(0).setText(nullToEmpty(row.residentName()));
                tableRow.getCell(1).setText(nullToEmpty(row.apartment()));
                tableRow.getCell(2).setText(row.date() != null ? row.date().format(DATE_FORMAT) : "");
                tableRow.getCell(3).setText(spaceLabel(row.spaceType()));
                rowCount++;
            }
        }

        @Override
        public void finish() throws IOException {
            if (rowCount == 0) {
                table.createRow().getCell(0).setText("Nenhuma reserva no período");
            }
            document.write(output);
            document.close();
        }
    }

    private static final class PdfSession implements ReportExporterSession {
        private final ReportContext context;
        private final OutputStream output;
        private final TemplateEngine templateEngine;
        private final StringBuilder rowsHtml = new StringBuilder();
        private int rowCount;

        PdfSession(ReportContext context, OutputStream output, TemplateEngine templateEngine) {
            this.context = context;
            this.output = output;
            this.templateEngine = templateEngine;
        }

        @Override
        public void addRows(List<MonthlyReservationReportDTO> rows) {
            for (MonthlyReservationReportDTO row : rows) {
                rowsHtml.append("<tr>")
                        .append("<td>").append(escapeHtml(row.residentName())).append("</td>")
                        .append("<td>").append(escapeHtml(row.apartment())).append("</td>")
                        .append("<td>").append(row.date() != null ? row.date().format(DATE_FORMAT) : "").append("</td>")
                        .append("<td>").append(escapeHtml(spaceLabel(row.spaceType()))).append("</td>")
                        .append("</tr>");
                rowCount++;
            }
        }

        @Override
        public void finish() throws IOException {
            if (rowCount == 0) {
                rowsHtml.append("<tr><td colspan=\"4\">Nenhuma reserva no período</td></tr>");
            }

            Context thymeleafContext = new Context();
            thymeleafContext.setVariable("title", context.title());
            thymeleafContext.setVariable("subtitle", context.subtitle());
            thymeleafContext.setVariable("rowsHtml", rowsHtml.toString());
            String html = templateEngine.process("report/reservation-monthly", thymeleafContext);

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFont(resolveFontFile(), PDF_FONT_FAMILY);
            builder.withHtmlContent(html, null);
            builder.toStream(output);
            builder.run();
        }
    }

    private static String spaceLabel(String spaceType) {
        if (spaceType == null || spaceType.isBlank()) {
            return "";
        }
        return SPACE_LABELS.getOrDefault(spaceType, spaceType);
    }

    private static String nullToEmpty(String value) {
        return value != null ? value : "";
    }

    private static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    static File resolveFontFile() {
        String configured = System.getProperty("app.report.font-file");
        if (configured != null && !configured.isBlank() && new File(configured).isFile()) {
            return new File(configured);
        }
        List<String> candidates = List.of(
                "/System/Library/Fonts/Supplemental/Arial.ttf",
                "/Library/Fonts/Arial.ttf",
                "/System/Library/Fonts/Supplemental/Arial Unicode.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "/usr/share/fonts/dejavu/DejaVuSans.ttf",
                "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf"
        );
        for (String candidate : candidates) {
            File font = new File(candidate);
            if (font.isFile()) {
                return font;
            }
        }
        throw new IllegalStateException("Nenhuma fonte adequada encontrada para geração do PDF. " +
                "Defina a propriedade -Dapp.report.font-file=<caminho.ttf> ou instale uma fonte (ex.: DejaVu).");
    }
}
