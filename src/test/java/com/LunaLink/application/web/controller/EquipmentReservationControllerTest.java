package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.facades.equipment.EquipmentReservationFacade;
import com.LunaLink.application.domain.enums.EquipmentReservationStatus;
import com.LunaLink.application.web.dto.EquipmentDTO.EquipmentReservationRequestDTO;
import com.LunaLink.application.web.dto.EquipmentDTO.EquipmentReservationResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipmentReservationControllerTest {

    @Mock
    private EquipmentReservationFacade facade;

    @InjectMocks
    private EquipmentReservationController controller;

    @Test
    @DisplayName("Deve criar reserva de equipamento")
    void createReservation_ShouldReturnCreated() {
        // Arrange
        EquipmentReservationRequestDTO requestDTO = new EquipmentReservationRequestDTO(1L, LocalDate.now(), LocalTime.now(), LocalTime.now().plusHours(1));
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test@email.com");

        EquipmentReservationResponseDTO responseDTO = new EquipmentReservationResponseDTO(UUID.randomUUID(), "TV", "User", "101", LocalDate.now(), LocalTime.now(), LocalTime.now().plusHours(1), EquipmentReservationStatus.CONFIRMED, LocalDateTime.now(), null, null);
        when(facade.createReservation(requestDTO, "test@email.com")).thenReturn(responseDTO);

        // Act
        ResponseEntity<EquipmentReservationResponseDTO> response = controller.createReservation(requestDTO, authentication);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Deve fazer handover de equipamento")
    void handoverEquipment_ShouldReturnOk() {
        // Arrange
        UUID id = UUID.randomUUID();
        EquipmentReservationResponseDTO responseDTO = new EquipmentReservationResponseDTO(id, "TV", "User", "101", LocalDate.now(), LocalTime.now(), LocalTime.now().plusHours(1), EquipmentReservationStatus.IN_USE, LocalDateTime.now(), LocalDateTime.now(), null);
        when(facade.handoverEquipment(id)).thenReturn(responseDTO);

        // Act
        ResponseEntity<EquipmentReservationResponseDTO> response = controller.handoverEquipment(id);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(EquipmentReservationStatus.IN_USE, response.getBody().status());
    }

    @Test
    @DisplayName("Deve fazer return de equipamento")
    void returnEquipment_ShouldReturnOk() {
        // Arrange
        UUID id = UUID.randomUUID();
        EquipmentReservationResponseDTO responseDTO = new EquipmentReservationResponseDTO(id, "TV", "User", "101", LocalDate.now(), LocalTime.now(), LocalTime.now().plusHours(1), EquipmentReservationStatus.RETURNED, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now());
        when(facade.returnEquipment(id)).thenReturn(responseDTO);

        // Act
        ResponseEntity<EquipmentReservationResponseDTO> response = controller.returnEquipment(id);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(EquipmentReservationStatus.RETURNED, response.getBody().status());
    }

    @Test
    @DisplayName("Deve listar reservas de equipamento")
    void listReservations_ShouldReturnList() {
        // Arrange
        LocalDate date = LocalDate.now();
        EquipmentReservationStatus status = EquipmentReservationStatus.CONFIRMED;
        List<EquipmentReservationResponseDTO> list = List.of(new EquipmentReservationResponseDTO(UUID.randomUUID(), "TV", "User", "101", date, LocalTime.now(), LocalTime.now().plusHours(1), status, LocalDateTime.now(), null, null));
        when(facade.listReservations(date, status)).thenReturn(list);

        // Act
        ResponseEntity<List<EquipmentReservationResponseDTO>> response = controller.listReservations(date, status);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }
}
