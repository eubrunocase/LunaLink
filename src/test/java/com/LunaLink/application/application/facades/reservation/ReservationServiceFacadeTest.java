package com.LunaLink.application.application.facades.reservation;

import com.LunaLink.application.application.ports.input.ReservationServicePort;
import com.LunaLink.application.application.ports.input.UserServicePort;
import com.LunaLink.application.domain.enums.UserRoles;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationCreateDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationRequestDTO;
import com.LunaLink.application.web.dto.UserDTO.ResponseUserDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceFacadeTest {

    @Mock
    private ReservationServicePort reservationService;

    @Mock
    private UserServicePort userServicePort;

    @InjectMocks
    private ReservationServiceFacade facade;

    @Test
    @DisplayName("Deve criar reserva para usuário autenticado")
    void createReservationForAuthenticatedUser_ShouldCallService_WhenValidData() {
        // Arrange
        String email = "testUser@email.com";
        ReservationCreateDTO createDTO = new ReservationCreateDTO(LocalDate.now(), 1L);
        ResponseUserDTO userDTO = new ResponseUserDTO(UUID.randomUUID(), "Test User", "101", email, UserRoles.RESIDENT_ROLE, null);

        when(userServicePort.findUserByEmail(email)).thenReturn(userDTO);

        // Act
        facade.createReservationForAuthenticatedUser(createDTO, email);

        // Assert
        verify(reservationService, times(1)).createReservation(any(ReservationRequestDTO.class));
    }

    @Test
    @DisplayName("Deve listar todas as reservas")
    void findAllReservations_ShouldCallService() {
        // Act
        facade.findAllReservations();

        // Assert
        verify(reservationService, times(1)).findAllReservations();
    }

    @Test
    @DisplayName("Deve buscar reserva por ID")
    void findReservationById_ShouldCallService() {
        // Arrange
        UUID id = UUID.randomUUID();

        // Act
        facade.findReservationById(id);

        // Assert
        verify(reservationService, times(1)).findReservationById(id);
    }

    @Test
    @DisplayName("Deve deletar reserva")
    void deleteReservation_ShouldCallService() {
        // Arrange
        UUID id = UUID.randomUUID();

        // Act
        facade.deleteReservation(id);

        // Assert
        verify(reservationService, times(1)).deleteReservation(id);
    }

    @Test
    @DisplayName("Deve atualizar reserva")
    void updateReservation_ShouldCallService() {
        // Arrange
        UUID id = UUID.randomUUID();
        ReservationRequestDTO requestDTO = new ReservationRequestDTO(UUID.randomUUID(), LocalDate.now(), 1L);

        // Act
        facade.updateReservation(id, requestDTO);

        // Assert
        verify(reservationService, times(1)).updateReservation(id, requestDTO);
    }

    @Test
    @DisplayName("Deve aprovar reserva")
    void approveReservation_ShouldCallService() {
        // Arrange
        UUID id = UUID.randomUUID();

        // Act
        facade.approveReservation(id);

        // Assert
        verify(reservationService, times(1)).approveReservation(id);
    }

    @Test
    @DisplayName("Deve rejeitar reserva")
    void rejectReservation_ShouldCallService() {
        // Arrange
        UUID id = UUID.randomUUID();

        // Act
        facade.rejectReservation(id);

        // Assert
        verify(reservationService, times(1)).rejectReservation(id);
    }

    @Test
    @DisplayName("Deve verificar disponibilidade")
    void checkAvaliability_ShouldCallService() {
        // Arrange
        LocalDate date = LocalDate.now();
        Long spaceId = 1L;
        UUID userId = UUID.randomUUID();

        // Act
        facade.checkAvaliability(date, spaceId, userId);

        // Assert
        verify(reservationService, times(1)).checkAvaliability(date, spaceId, userId);
    }
}
