package com.LunaLink.application.application.service.equipment;

import com.LunaLink.application.application.ports.output.EquipmentReservationRepositoryPort;
import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.domain.enums.EquipmentReservationStatus;
import com.LunaLink.application.domain.enums.UserRoles;
import com.LunaLink.application.domain.model.equipment.Equipment;
import com.LunaLink.application.domain.model.equipment.EquipmentReservation;
import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.infrastructure.repository.equipment.EquipmentRepository;
import com.LunaLink.application.web.dto.EquipmentDTO.EquipmentReservationRequestDTO;
import com.LunaLink.application.web.dto.EquipmentDTO.EquipmentReservationResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipmentReservationServiceTest {

    @Mock
    private EquipmentReservationRepositoryPort reservationRepositoryPort;
    @Mock
    private EquipmentRepository equipmentRepository;
    @Mock
    private UserRepositoryPort userRepository;

    @InjectMocks
    private EquipmentReservationService service;

    private Users user;
    private Equipment tv;
    private EquipmentReservationRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        user = new Users("User", "101", "user@email.com", "pass", UserRoles.RESIDENT_ROLE);
        tv = new Equipment("Televisão Comunitária");
        tv.setId(1L);
        requestDTO = new EquipmentReservationRequestDTO(1L, LocalDate.now().plusDays(1), LocalTime.of(14, 0), LocalTime.of(16, 0));
    }

    @Test
    @DisplayName("Deve criar reserva com sucesso quando horário livre")
    void createReservation_ShouldSucceed_WhenTimeIsAvailable() {
        // Arrange
        when(userRepository.findByEmail("user@email.com")).thenReturn(user);
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(tv));
        when(reservationRepositoryPort.hasConflict(any(), any(), any(), any(), anyList())).thenReturn(false);
        
        EquipmentReservation savedReservation = new EquipmentReservation(tv, user, requestDTO.date(), requestDTO.startTime(), requestDTO.endTime());
        when(reservationRepositoryPort.save(any(EquipmentReservation.class))).thenReturn(savedReservation);

        // Act
        EquipmentReservationResponseDTO result = service.createReservation(requestDTO, "user@email.com");

        // Assert
        assertNotNull(result);
        assertEquals(EquipmentReservationStatus.CONFIRMED, result.status());
        verify(reservationRepositoryPort).save(any(EquipmentReservation.class));
    }

    @Test
    @DisplayName("Deve falhar ao criar reserva com conflito de horário")
    void createReservation_ShouldFail_WhenTimeHasConflict() {
        // Arrange
        when(userRepository.findByEmail("user@email.com")).thenReturn(user);
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(tv));
        when(reservationRepositoryPort.hasConflict(any(), any(), any(), any(), anyList())).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.createReservation(requestDTO, "user@email.com"));
        
        assertEquals("O equipamento já está reservado neste horário.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve entregar equipamento e mudar status para IN_USE")
    void handoverEquipment_ShouldChangeStatus_ToInUse() {
        // Arrange
        UUID reservationId = UUID.randomUUID();
        EquipmentReservation reservation = new EquipmentReservation(tv, user, LocalDate.now(), LocalTime.now(), LocalTime.now().plusHours(2));
        reservation.setStatus(EquipmentReservationStatus.CONFIRMED);
        
        when(reservationRepositoryPort.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationRepositoryPort.save(any(EquipmentReservation.class))).thenReturn(reservation);

        // Act
        service.handoverEquipment(reservationId);

        // Assert
        assertEquals(EquipmentReservationStatus.IN_USE, reservation.getStatus());
        assertNotNull(reservation.getPickedUpAt());
    }

    @Test
    @DisplayName("Deve listar apenas as reservas do usuário logado")
    void listMyReservations_ShouldReturnOnlyUserReservations() {
        // Arrange
        UUID userId = UUID.randomUUID();
        user.setId(userId);

        EquipmentReservation reservation1 = new EquipmentReservation(tv, user, LocalDate.now().plusDays(1), LocalTime.of(14, 0), LocalTime.of(16, 0));
        reservation1.setStatus(EquipmentReservationStatus.CONFIRMED);
        EquipmentReservation reservation2 = new EquipmentReservation(tv, user, LocalDate.now().plusDays(2), LocalTime.of(18, 0), LocalTime.of(20, 0));
        reservation2.setStatus(EquipmentReservationStatus.RETURNED);

        when(userRepository.findByEmail("user@email.com")).thenReturn(user);
        when(reservationRepositoryPort.findAllByUserId(userId)).thenReturn(List.of(reservation1, reservation2));

        // Act
        List<EquipmentReservationResponseDTO> result = service.listMyReservations("user@email.com");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(EquipmentReservationStatus.CONFIRMED, result.get(0).status());
        verify(reservationRepositoryPort).findAllByUserId(userId);
    }

    @Test
    @DisplayName("Deve devolver equipamento e mudar status para RETURNED")
    void returnEquipment_ShouldChangeStatus_ToReturned() {
        // Arrange
        UUID reservationId = UUID.randomUUID();
        EquipmentReservation reservation = new EquipmentReservation(tv, user, LocalDate.now(), LocalTime.now(), LocalTime.now().plusHours(2));
        reservation.setStatus(EquipmentReservationStatus.IN_USE);
        
        when(reservationRepositoryPort.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationRepositoryPort.save(any(EquipmentReservation.class))).thenReturn(reservation);

        // Act
        service.returnEquipment(reservationId);

        // Assert
        assertEquals(EquipmentReservationStatus.RETURNED, reservation.getStatus());
        assertNotNull(reservation.getReturnedAt());
    }

    @Test
    @DisplayName("Deve cancelar reserva CONFIRMED quando o dono cancela")
    void cancelEquipmentReservation_ShouldCancel_WhenConfirmedByOwner() {
        // Arrange
        UUID reservationId = UUID.randomUUID();
        EquipmentReservation reservation = new EquipmentReservation(tv, user, LocalDate.now(), LocalTime.now(), LocalTime.now().plusHours(2));
        reservation.setStatus(EquipmentReservationStatus.CONFIRMED);

        when(userRepository.findByEmail("user@email.com")).thenReturn(user);
        when(reservationRepositoryPort.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationRepositoryPort.save(any(EquipmentReservation.class))).thenReturn(reservation);

        // Act
        EquipmentReservationResponseDTO result = service.cancelEquipmentReservation(reservationId, "user@email.com");

        // Assert
        assertEquals(EquipmentReservationStatus.CANCELED, result.status());
        assertEquals(EquipmentReservationStatus.CANCELED, reservation.getStatus());
        assertNotNull(reservation.getCanceledAt());
    }

    @Test
    @DisplayName("Deve permitir que Admin/Funcionário cancele reserva de outro morador")
    void cancelEquipmentReservation_ShouldAllow_WhenManager() {
        // Arrange
        UUID reservationId = UUID.randomUUID();
        Users otherUser = new Users("Outro", "202", "other@email.com", "pass", UserRoles.RESIDENT_ROLE);
        Users employee = new Users("Porteiro", "000", "employee@email.com", "pass", UserRoles.EMPLOYEE);

        EquipmentReservation reservation = new EquipmentReservation(tv, otherUser, LocalDate.now(), LocalTime.now(), LocalTime.now().plusHours(2));
        reservation.setStatus(EquipmentReservationStatus.CONFIRMED);

        when(userRepository.findByEmail("employee@email.com")).thenReturn(employee);
        when(reservationRepositoryPort.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationRepositoryPort.save(any(EquipmentReservation.class))).thenReturn(reservation);

        // Act
        EquipmentReservationResponseDTO result = service.cancelEquipmentReservation(reservationId, "employee@email.com");

        // Assert
        assertEquals(EquipmentReservationStatus.CANCELED, result.status());
        assertNotNull(reservation.getCanceledAt());
    }

    @Test
    @DisplayName("Deve bloquear cancelamento de reserva de outro morador")
    void cancelEquipmentReservation_ShouldFail_WhenNotOwner() {
        // Arrange
        UUID reservationId = UUID.randomUUID();
        Users otherUser = new Users("Outro", "202", "other@email.com", "pass", UserRoles.RESIDENT_ROLE);

        EquipmentReservation reservation = new EquipmentReservation(tv, otherUser, LocalDate.now(), LocalTime.now(), LocalTime.now().plusHours(2));
        reservation.setStatus(EquipmentReservationStatus.CONFIRMED);

        when(userRepository.findByEmail("user@email.com")).thenReturn(user);
        when(reservationRepositoryPort.findById(reservationId)).thenReturn(Optional.of(reservation));

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> service.cancelEquipmentReservation(reservationId, "user@email.com"));

        assertEquals("Você não pode cancelar uma reserva de outro morador.", exception.getMessage());
        verify(reservationRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Deve bloquear cancelamento de reserva não CONFIRMED")
    void cancelEquipmentReservation_ShouldFail_WhenNotConfirmed() {
        // Arrange
        UUID reservationId = UUID.randomUUID();
        EquipmentReservation reservation = new EquipmentReservation(tv, user, LocalDate.now(), LocalTime.now(), LocalTime.now().plusHours(2));
        reservation.setStatus(EquipmentReservationStatus.IN_USE);

        when(reservationRepositoryPort.findById(reservationId)).thenReturn(Optional.of(reservation));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.cancelEquipmentReservation(reservationId, "user@email.com"));

        assertEquals("Apenas reservas CONFIRMED podem ser canceladas. Status atual: " + EquipmentReservationStatus.IN_USE, exception.getMessage());
        verify(reservationRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar ao cancelar reserva inexistente")
    void cancelEquipmentReservation_ShouldFail_WhenNotFound() {
        // Arrange
        UUID reservationId = UUID.randomUUID();
        when(reservationRepositoryPort.findById(reservationId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.cancelEquipmentReservation(reservationId, "user@email.com"));

        assertEquals("Reserva não encontrada.", exception.getMessage());
        verify(reservationRepositoryPort, never()).save(any());
    }
}
