package com.LunaLink.application.application.ports.output;

import com.LunaLink.application.application.service.report.ReportContext;
import com.LunaLink.application.web.dto.ReservationsDTO.MonthlyReservationReportDTO;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Porta de saída para geração de documentos de relatório (DOCX/PDF).
 * <p>
 * O estado do documento é mantido em um {@link ReportExporterSession} (criado por {@link #begin}),
 * de modo que a escrita pode ser feita <b>por página</b> (streaming), sem materializar todo o
 * relatório em memória e de forma segura para jobs concorrentes.
 */
public interface ReportExporterPort {

    ReportExporterSession begin(ReportContext context, OutputStream output) throws IOException;

    interface ReportExporterSession {
        void addRows(List<MonthlyReservationReportDTO> rows) throws IOException;

        void finish() throws IOException;
    }
}
