package com.LunaLink.application.application.facades.equipment;

import com.LunaLink.application.application.ports.input.EquipmentReservationServicePort;
import com.LunaLink.application.domain.enums.EquipmentReservationStatus;
import com.LunaLink.application.web.dto.EquipmentDTO.EquipmentReservationRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipmentReservationFacadeTest {

    @Mock
    private EquipmentReservationServicePort servicePort;

    @InjectMocks
    private EquipmentReservationFacade facade;

    @Test
    @DisplayName("Deve criar reserva de equipamento")
    void createReservation_ShouldCallService() {
        // Arrange
        EquipmentReservationRequestDTO requestDTO = new EquipmentReservationRequestDTO(1L, LocalDate.now(), LocalTime.now(), LocalTime.now().plusHours(1));
        String email = "test@email.com";

        // Act
        facade.createReservation(requestDTO, email);

        // Assert
        verify(servicePort, times(1)).createReservation(requestDTO, email);
    }

    @Test
    @DisplayName("Deve fazer handover de equipamento")
    void handoverEquipment_ShouldCallService() {
        // Arrange
        UUID id = UUID.randomUUID();

        // Act
        facade.handoverEquipment(id);

        // Assert
        verify(servicePort, times(1)).handoverEquipment(id);
    }

    @Test
    @DisplayName("Deve fazer return de equipamento")
    void returnEquipment_ShouldCallService() {
        // Arrange
        UUID id = UUID.randomUUID();

        // Act
        facade.returnEquipment(id);

        // Assert
        verify(servicePort, times(1)).returnEquipment(id);
    }

    @Test
    @DisplayName("Deve listar reservas de equipamento")
    void listReservations_ShouldCallService() {
        // Arrange
        LocalDate date = LocalDate.now();
        EquipmentReservationStatus status = EquipmentReservationStatus.CONFIRMED;

        // Act
        facade.listReservations(date, status);

        // Assert
        verify(servicePort, times(1)).listReservations(date, status);
    }
}
