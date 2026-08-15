package com.LunaLink.application.application.service.report;

import com.LunaLink.application.domain.enums.ReportFormat;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record ReportContext(int month, int year, ReportFormat format, String condominiumName, Instant generatedAt) {

    private static final List<String> MONTHS = List.of(
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro");

    private static final DateTimeFormatter GENERATED_AT_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

    public String title() {
        return "Relatório de Reservas Tarifadas — " + MONTHS.get(month - 1) + "/" + year;
    }

    public String subtitle() {
        return "Condomínio " + condominiumName + " · Gerado em " + GENERATED_AT_FORMAT.format(generatedAt);
    }
}
