package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.facades.reservation.ReservationServiceFacade;
import com.LunaLink.application.application.service.report.ReportExportJob;
import com.LunaLink.application.domain.enums.ReportExportStatus;
import com.LunaLink.application.domain.enums.ReportFormat;
import com.LunaLink.application.web.dto.ReservationsDTO.MonthlyReservationReportDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReportExportJobResponseDTO;
import com.LunaLink.application.web.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReservationReportControllerWebTest {

    private MockMvc mockMvc;
    private ReservationServiceFacade facade;

    @BeforeEach
    void setUp() {
        facade = mock(ReservationServiceFacade.class);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "admin@email.com", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN_ROLE")));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new ReservationController(facade, null))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("CA03: GET /lunaLink/reservation/report/monthly retorna 200 com dados do morador e reserva")
    void getMonthlyReport_shouldReturn200WithExpectedFields() throws Exception {
        MonthlyReservationReportDTO report = new MonthlyReservationReportDTO(
                "Maria Silva", "102", LocalDate.of(2026, 5, 15), "CHURRASQUEIRA");
        when(facade.generateMonthlyReport(5, 2026)).thenReturn(List.of(report));

        mockMvc.perform(get("/lunaLink/reservation/report/monthly")
                        .param("month", "5")
                        .param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].residentName").value("Maria Silva"))
                .andExpect(jsonPath("$[0].apartment").value("102"))
                .andExpect(jsonPath("$[0].date").value("2026-05-15"))
                .andExpect(jsonPath("$[0].spaceType").value("CHURRASQUEIRA"));
    }

    @Test
    @DisplayName("CA01: mês inválido retorna 400 Bad Request")
    void getMonthlyReport_withInvalidMonth_shouldReturn400() throws Exception {
        when(facade.generateMonthlyReport(13, 2026))
                .thenThrow(new IllegalArgumentException("Mês inválido: 13"));

        mockMvc.perform(get("/lunaLink/reservation/report/monthly")
                        .param("month", "13")
                        .param("year", "2026"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("CA01: sem reservas no período retorna 200 com lista vazia")
    void getMonthlyReport_withNoReservations_shouldReturn200EmptyList() throws Exception {
        when(facade.generateMonthlyReport(1, 2026)).thenReturn(List.of());

        mockMvc.perform(get("/lunaLink/reservation/report/monthly")
                        .param("month", "1")
                        .param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("CA01: POST /lunaLink/reservation/report/monthly/export retorna 202 com jobId")
    void createMonthlyReportExport_shouldReturn202WithJobId() throws Exception {
        ReportExportJobResponseDTO response = new ReportExportJobResponseDTO(
                "job-123", ReportExportStatus.PROCESSING, null);
        when(facade.createMonthlyReportExport(5, 2026, ReportFormat.PDF)).thenReturn(response);

        mockMvc.perform(post("/lunaLink/reservation/report/monthly/export")
                        .param("month", "5")
                        .param("year", "2026")
                        .param("format", "PDF"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").value("job-123"))
                .andExpect(jsonPath("$.status").value("PROCESSING"));
    }

    @Test
    @DisplayName("CA01: GET status do job de exportação retorna o status atual")
    void getMonthlyReportExportStatus_shouldReturnJobStatus() throws Exception {
        ReportExportJobResponseDTO response = new ReportExportJobResponseDTO(
                "job-123", ReportExportStatus.READY, null);
        when(facade.getMonthlyReportExportStatus("job-123")).thenReturn(response);

        mockMvc.perform(get("/lunaLink/reservation/report/monthly/export/job-123/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value("job-123"))
                .andExpect(jsonPath("$.status").value("READY"));
    }

    @Test
    @DisplayName("CA01: GET download do relatório pronto retorna arquivo com Content-Disposition")
    void downloadMonthlyReportExport_shouldReturnFileContent() throws Exception {
        Path tempFile = Files.createTempFile("relatorio-reservas", ".pdf");
        Files.write(tempFile, new byte[]{0x25, 0x50, 0x44, 0x46}); // %PDF

        ReportExportJob job = ReportExportJob.create(5, 2026, ReportFormat.PDF).markCompleted();
        job.setTempFile(tempFile);
        when(facade.getMonthlyReportExportFile(job.getId())).thenReturn(job);

        mockMvc.perform(get("/lunaLink/reservation/report/monthly/export/{jobId}", job.getId()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"relatorio-reservas-05-2026.pdf\""))
                .andExpect(content().bytes(new byte[]{0x25, 0x50, 0x44, 0x46}));

        Files.deleteIfExists(tempFile);
    }

    @Test
    @DisplayName("CA01: job não pronto lança exceção tratada como 409")
    void downloadMonthlyReportExport_whenNotReady_shouldReturn409() throws Exception {
        when(facade.getMonthlyReportExportFile("job-123"))
                .thenThrow(new IllegalStateException("O relatório ainda não está pronto para download."));

        mockMvc.perform(get("/lunaLink/reservation/report/monthly/export/job-123"))
                .andExpect(status().isConflict());
    }
}
