package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.facades.occurrence.OccurrenceFacade;
import com.LunaLink.application.web.dto.OccurrenceDTO.OccurrenceCreateRequestDTO;
import com.LunaLink.application.web.dto.OccurrenceDTO.OccurrenceResponseDTO;
import com.LunaLink.application.web.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OccurrenceControllerWebTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private OccurrenceFacade facade;
    private UsernamePasswordAuthenticationToken authentication;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        facade = mock(OccurrenceFacade.class);

        authentication = new UsernamePasswordAuthenticationToken(
                "resident@email.com", null, List.of(new SimpleGrantedAuthority("ROLE_RESIDENT_ROLE")));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new OccurrenceController(facade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("CA01: POST /lunaLink/occurrences valido retorna 201 Created")
    void create_shouldReturn201() throws Exception {
        OccurrenceCreateRequestDTO dto = new OccurrenceCreateRequestDTO("Barulho excessivo", LocalDateTime.of(2026, 8, 13, 20, 30));
        OccurrenceResponseDTO response = new OccurrenceResponseDTO(UUID.randomUUID(), "Morador", "Barulho excessivo",
                dto.incidentDate(), LocalDateTime.now());

        when(facade.createOccurrence(eq(dto), eq("resident@email.com"))).thenReturn(response);

        mockMvc.perform(post("/lunaLink/occurrences")
                        .with(request -> {
                            request.setUserPrincipal(authentication);
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Barulho excessivo"));
    }

    @Test
    @DisplayName("CA01: POST com incidentDate no formato do frontend (data e hora local) retorna 201")
    void create_withLocalDateTimePayload_shouldReturn201() throws Exception {
        String body = "{\"description\":\"Barulho excessivo\",\"incidentDate\":\"2026-08-13T20:30:00\"}";

        when(facade.createOccurrence(any(), any())).thenReturn(
                new OccurrenceResponseDTO(UUID.randomUUID(), "Morador", "Barulho excessivo",
                        LocalDateTime.of(2026, 8, 13, 20, 30), LocalDateTime.now()));

        mockMvc.perform(post("/lunaLink/occurrences")
                        .with(request -> {
                            request.setUserPrincipal(authentication);
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Barulho excessivo"));
    }

    @Test
    @DisplayName("CA04: POST sem descricao retorna 400 Bad Request com validationErrors")
    void create_withoutDescription_shouldReturn400() throws Exception {
        String body = "{\"incidentDate\":\"2026-08-13T20:30:00\"}";

        mockMvc.perform(post("/lunaLink/occurrences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.description").exists());
    }

    @Test
    @DisplayName("CA04: POST sem incidentDate retorna 400 Bad Request com validationErrors")
    void create_withoutIncidentDate_shouldReturn400() throws Exception {
        String body = "{\"description\":\"Barulho excessivo\"}";

        mockMvc.perform(post("/lunaLink/occurrences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.incidentDate").exists());
    }

    @Test
    @DisplayName("CA04: POST com data futura retorna 400 Bad Request (regra de negocio)")
    void create_withFutureDate_shouldReturn400() throws Exception {
        OccurrenceCreateRequestDTO dto = new OccurrenceCreateRequestDTO("Barulho excessivo", LocalDateTime.now().plusDays(1));

        when(facade.createOccurrence(any(), any())).thenThrow(
                new IllegalArgumentException("A data do incidente não pode ser no futuro."));

        mockMvc.perform(post("/lunaLink/occurrences")
                        .with(request -> {
                            request.setUserPrincipal(authentication);
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
