package com.LunaLink.application.infrastructure.security;

import com.LunaLink.application.application.facades.reservation.ReservationServiceFacade;
import com.LunaLink.application.application.ports.input.UserServicePort;
import com.LunaLink.application.application.ports.output.TokenBlacklistRepositoryPort;
import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.application.service.auth.TokenService;
import com.LunaLink.application.application.service.report.ReportExportJob;
import com.LunaLink.application.domain.enums.ReportExportStatus;
import com.LunaLink.application.domain.enums.ReportFormat;
import com.LunaLink.application.web.controller.ReservationController;
import com.LunaLink.application.web.dto.ReservationsDTO.ReportExportJobResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Path;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
@Import({SecurityConfiguration.class, RestAuthenticationEntryPoint.class, SecurityFilter.class})
class ReservationReportSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationServiceFacade reservationServiceFacade;

    @MockitoBean
    private UserServicePort userServicePort;

    @MockitoBean
    private TokenAuthenticator tokenAuthenticator;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private TokenBlacklistRepositoryPort tokenBlacklistRepositoryPort;

    @MockitoBean
    private UserRepositoryPort userRepositoryPort;

    @Test
    @DisplayName("CA01: Administrador com ADMIN_ROLE acessa o relatório mensal com sucesso")
    @WithMockUser(username = "admin@email.com", roles = "ADMIN_ROLE")
    void admin_shouldAccessMonthlyReport() throws Exception {
        when(reservationServiceFacade.generateMonthlyReport(5, 2026)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/lunaLink/reservation/report/monthly")
                        .param("month", "5")
                        .param("year", "2026"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CA01: Morador com RESIDENT_ROLE é bloqueado no relatório mensal")
    @WithMockUser(username = "resident@email.com", roles = "RESIDENT_ROLE")
    void resident_shouldBeForbiddenFromMonthlyReport() throws Exception {
        mockMvc.perform(get("/lunaLink/reservation/report/monthly")
                        .param("month", "5")
                        .param("year", "2026"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Não autenticado é bloqueado no relatório mensal")
    void unauthenticated_shouldBeBlockedFromMonthlyReport() throws Exception {
        mockMvc.perform(get("/lunaLink/reservation/report/monthly")
                        .param("month", "5")
                        .param("year", "2026"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("CA01: Administrador com ADMIN_ROLE cria exportação do relatório com sucesso")
    @WithMockUser(username = "admin@email.com", roles = "ADMIN_ROLE")
    void admin_shouldCreateReportExport() throws Exception {
        when(reservationServiceFacade.createMonthlyReportExport(5, 2026, ReportFormat.PDF))
                .thenReturn(new ReportExportJobResponseDTO("job-123", ReportExportStatus.PROCESSING, null));

        mockMvc.perform(post("/lunaLink/reservation/report/monthly/export")
                        .param("month", "5")
                        .param("year", "2026")
                        .param("format", "PDF"))
                .andExpect(status().isAccepted());
    }

    @Test
    @DisplayName("CA01: Morador com RESIDENT_ROLE é bloqueado ao criar exportação do relatório")
    @WithMockUser(username = "resident@email.com", roles = "RESIDENT_ROLE")
    void resident_shouldBeForbiddenFromReportExport() throws Exception {
        mockMvc.perform(post("/lunaLink/reservation/report/monthly/export")
                        .param("month", "5")
                        .param("year", "2026")
                        .param("format", "PDF"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CA01: Administrador com ADMIN_ROLE consulta o status do job de exportação")
    @WithMockUser(username = "admin@email.com", roles = "ADMIN_ROLE")
    void admin_shouldAccessReportExportStatus() throws Exception {
        when(reservationServiceFacade.getMonthlyReportExportStatus("job-123"))
                .thenReturn(new ReportExportJobResponseDTO("job-123", ReportExportStatus.READY, null));

        mockMvc.perform(get("/lunaLink/reservation/report/monthly/export/job-123/status"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CA01: Administrador com ADMIN_ROLE baixa o relatório exportado")
    @WithMockUser(username = "admin@email.com", roles = "ADMIN_ROLE")
    void admin_shouldDownloadReportExport() throws Exception {
        ReportExportJob job = ReportExportJob.create(5, 2026, ReportFormat.PDF).markCompleted();
        job.setTempFile(Path.of("/tmp/relatorio-reservas-05-2026.pdf"));
        when(reservationServiceFacade.getMonthlyReportExportFile("job-123")).thenReturn(job);

        mockMvc.perform(get("/lunaLink/reservation/report/monthly/export/job-123"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CA01: Morador com RESIDENT_ROLE é bloqueado ao baixar o relatório exportado")
    @WithMockUser(username = "resident@email.com", roles = "RESIDENT_ROLE")
    void resident_shouldBeForbiddenFromDownloadReportExport() throws Exception {
        mockMvc.perform(get("/lunaLink/reservation/report/monthly/export/job-123"))
                .andExpect(status().isForbidden());
    }
}
