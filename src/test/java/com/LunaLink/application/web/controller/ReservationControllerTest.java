package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.facades.reservation.ReservationServiceFacade;
import com.LunaLink.application.application.ports.input.UserServicePort;
import com.LunaLink.application.domain.enums.ReservationStatus;
import com.LunaLink.application.domain.enums.UserRoles;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationCreateDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationRequestDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.ReservationResponseDTO;
import com.LunaLink.application.web.dto.UserDTO.ResponseUserDTO;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationControllerTest {

    @Mock
    private ReservationServiceFacade facade;

    @Mock
    private UserServicePort userServicePort;

    @InjectMocks
    private ReservationController controller;

    @Test
    @DisplayName("Deve criar nova reserva")
    void createNewReservation_ShouldReturnCreated() {
        // Arrange
        ReservationCreateDTO createDTO = new ReservationCreateDTO(LocalDate.now(), 1L);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("testUser@email.com");

        ReservationResponseDTO responseDTO = new ReservationResponseDTO(UUID.randomUUID(), LocalDate.now(), null, null, ReservationStatus.PENDING, LocalDateTime.now(), null);
        when(facade.createReservationForAuthenticatedUser(createDTO, "testUser@email.com")).thenReturn(responseDTO);

        // Act
        ResponseEntity<ReservationResponseDTO> response = controller.createNewReservation(createDTO, authentication);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Deve listar reservas")
    void listReservations_ShouldReturnList() {
        // Arrange
        List<ReservationResponseDTO> reservations = List.of(new ReservationResponseDTO(UUID.randomUUID(), LocalDate.now(), null, null, ReservationStatus.PENDING, LocalDateTime.now(), null));
        when(facade.findAllReservations()).thenReturn(reservations);

        // Act
        ResponseEntity<List<ReservationResponseDTO>> response = controller.listReservations();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("Deve deletar reserva")
    void deleteReservation_ShouldReturnNoContent() {
        // Arrange
        UUID id = UUID.randomUUID();

        // Act
        ResponseEntity<Void> response = controller.deleteReservation(id);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(facade, times(1)).deleteReservation(id);
    }

    @Test
    @DisplayName("Deve buscar reserva por ID")
    void findReservationById_ShouldReturnReservation() {
        // Arrange
        UUID id = UUID.randomUUID();
        ReservationResponseDTO responseDTO = new ReservationResponseDTO(id, LocalDate.now(), null, null, ReservationStatus.PENDING, LocalDateTime.now(), null);
        when(facade.findReservationById(id)).thenReturn(responseDTO);

        // Act
        ResponseEntity<ReservationResponseDTO> response = controller.findReservationById(id);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(id, response.getBody().id());
    }

    @Test
    @DisplayName("Deve atualizar reserva")
    void updateReservation_ShouldReturnUpdated() {
        // Arrange
        UUID id = UUID.randomUUID();
        ReservationRequestDTO requestDTO = new ReservationRequestDTO(UUID.randomUUID(), LocalDate.now(), 1L);
        ReservationResponseDTO responseDTO = new ReservationResponseDTO(id, LocalDate.now(), null, null, ReservationStatus.PENDING, LocalDateTime.now(), null);
        when(facade.updateReservation(id, requestDTO)).thenReturn(responseDTO);

        // Act
        ResponseEntity<ReservationResponseDTO> response = controller.updateReservation(id, requestDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(id, response.getBody().id());
    }

    @Test
    @DisplayName("Deve verificar disponibilidade")
    void checkAvaliability_ShouldReturnTrue_WhenAvailable() {
        // Arrange
        LocalDate date = LocalDate.now();
        Long spaceId = 1L;
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("testUser@email.com");

        ResponseUserDTO userDTO = new ResponseUserDTO(UUID.randomUUID(), "Test User", "101", "testUser@email.com", UserRoles.RESIDENT_ROLE, null);
        when(userServicePort.findUserByEmail("testUser@email.com")).thenReturn(userDTO);
        when(facade.checkAvaliability(date, spaceId, userDTO.id())).thenReturn(true);

        // Act
        ResponseEntity<Boolean> response = controller.checkAvaliability(date, authentication, spaceId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
    }

    @Test
    @DisplayName("Deve aprovar reserva")
    void approveReservation_ShouldReturnApproved() {
        // Arrange
        UUID id = UUID.randomUUID();
        ReservationResponseDTO responseDTO = new ReservationResponseDTO(id, LocalDate.now(), null, null, ReservationStatus.APPROVED, LocalDateTime.now(), null);
        when(facade.approveReservation(id)).thenReturn(responseDTO);

        // Act
        ResponseEntity<ReservationResponseDTO> response = controller.approveReservation(id);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ReservationStatus.APPROVED, response.getBody().status());
    }

    @Test
    @DisplayName("Deve rejeitar reserva")
    void rejectReservation_ShouldReturnRejected() {
        // Arrange
        UUID id = UUID.randomUUID();
        ReservationResponseDTO responseDTO = new ReservationResponseDTO(id, LocalDate.now(), null, null, ReservationStatus.REJECTED, LocalDateTime.now(), null);
        when(facade.rejectReservation(id)).thenReturn(responseDTO);

        // Act
        ResponseEntity<ReservationResponseDTO> response = controller.rejectReservation(id);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ReservationStatus.REJECTED, response.getBody().status());
    }
}
