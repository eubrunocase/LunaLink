package com.LunaLink.application.infrastructure.security;

import com.LunaLink.application.application.facades.equipment.EquipmentReservationFacade;
import com.LunaLink.application.application.ports.output.TokenBlacklistRepositoryPort;
import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.application.service.auth.TokenService;
import com.LunaLink.application.web.controller.EquipmentReservationController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EquipmentReservationController.class)
@Import({SecurityConfiguration.class, RestAuthenticationEntryPoint.class, SecurityFilter.class})
class EquipmentReservationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EquipmentReservationFacade equipmentReservationFacade;

    @MockitoBean
    private TokenAuthenticator tokenAuthenticator;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private TokenBlacklistRepositoryPort tokenBlacklistRepositoryPort;

    @MockitoBean
    private UserRepositoryPort userRepositoryPort;

    private static final UUID RESERVATION_ID = UUID.fromString("a1a2a3a4-a1a2-a1a2-a1a2-a1a2a3a4a5a6");

    private static final String CREATE_BODY = """
            {
              "equipmentId": 1,
              "date": "2026-09-01",
              "startTime": "14:00:00",
              "endTime": "16:00:00"
            }
            """;

    @Test
    @DisplayName("Funcionário com EMPLOYEE lista reservas de equipamento")
    @WithMockUser(username = "employee@email.com", roles = "EMPLOYEE")
    void employee_shouldListEquipmentReservations() throws Exception {
        mockMvc.perform(get("/lunaLink/equipment-reservation"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Funcionário com EMPLOYEE faz handover do equipamento")
    @WithMockUser(username = "employee@email.com", roles = "EMPLOYEE")
    void employee_shouldHandoverEquipment() throws Exception {
        mockMvc.perform(patch("/lunaLink/equipment-reservation/{id}/handover", RESERVATION_ID))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Funcionário com EMPLOYEE registra a devolução do equipamento")
    @WithMockUser(username = "employee@email.com", roles = "EMPLOYEE")
    void employee_shouldReturnEquipment() throws Exception {
        mockMvc.perform(patch("/lunaLink/equipment-reservation/{id}/return", RESERVATION_ID))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Administrador com ADMIN_ROLE lista e faz handover do equipamento")
    @WithMockUser(username = "admin@email.com", roles = "ADMIN_ROLE")
    void admin_shouldAccessEquipmentReservationManagement() throws Exception {
        mockMvc.perform(get("/lunaLink/equipment-reservation"))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/lunaLink/equipment-reservation/{id}/handover", RESERVATION_ID))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/lunaLink/equipment-reservation/{id}/return", RESERVATION_ID))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Morador com RESIDENT_ROLE cria reserva de equipamento")
    @WithMockUser(username = "resident@email.com", roles = "RESIDENT_ROLE")
    void resident_shouldCreateEquipmentReservation() throws Exception {
        mockMvc.perform(post("/lunaLink/equipment-reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_BODY))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Morador com RESIDENT_ROLE é bloqueado na listagem de reservas de equipamento")
    @WithMockUser(username = "resident@email.com", roles = "RESIDENT_ROLE")
    void resident_shouldBeForbiddenFromListing() throws Exception {
        mockMvc.perform(get("/lunaLink/equipment-reservation"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Morador com RESIDENT_ROLE lista as próprias reservas de equipamento")
    @WithMockUser(username = "resident@email.com", roles = "RESIDENT_ROLE")
    void resident_shouldListOwnEquipmentReservations() throws Exception {
        mockMvc.perform(get("/lunaLink/equipment-reservation/mine"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Morador com RESIDENT_ROLE é bloqueado no handover/devolução do equipamento")
    @WithMockUser(username = "resident@email.com", roles = "RESIDENT_ROLE")
    void resident_shouldBeForbiddenFromHandoverAndReturn() throws Exception {
        mockMvc.perform(patch("/lunaLink/equipment-reservation/{id}/handover", RESERVATION_ID))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/lunaLink/equipment-reservation/{id}/return", RESERVATION_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Morador com RESIDENT_ROLE cancela a própria reserva de equipamento")
    @WithMockUser(username = "resident@email.com", roles = "RESIDENT_ROLE")
    void resident_shouldCancelOwnEquipmentReservation() throws Exception {
        mockMvc.perform(patch("/lunaLink/equipment-reservation/{id}/cancel", RESERVATION_ID))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Funcionário com EMPLOYEE cancela reserva de equipamento (gestão)")
    @WithMockUser(username = "employee@email.com", roles = "EMPLOYEE")
    void employee_shouldCancelEquipmentReservation() throws Exception {
        mockMvc.perform(patch("/lunaLink/equipment-reservation/{id}/cancel", RESERVATION_ID))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Administrador com ADMIN_ROLE cancela reserva de equipamento (gestão)")
    @WithMockUser(username = "admin@email.com", roles = "ADMIN_ROLE")
    void admin_shouldCancelEquipmentReservation() throws Exception {
        mockMvc.perform(patch("/lunaLink/equipment-reservation/{id}/cancel", RESERVATION_ID))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Não autenticado é bloqueado em todos os endpoints de equipamento")
    void unauthenticated_shouldBeBlocked() throws Exception {
        mockMvc.perform(get("/lunaLink/equipment-reservation"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(patch("/lunaLink/equipment-reservation/{id}/handover", RESERVATION_ID))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(patch("/lunaLink/equipment-reservation/{id}/cancel", RESERVATION_ID))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/lunaLink/equipment-reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_BODY))
                .andExpect(status().isUnauthorized());
    }
}
