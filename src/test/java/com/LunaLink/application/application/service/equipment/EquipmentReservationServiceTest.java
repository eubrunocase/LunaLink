package com.LunaLink.application.application.service.equipment;

import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.domain.enums.EquipmentReservationStatus;
import com.LunaLink.application.domain.enums.UserRoles;
import com.LunaLink.application.domain.model.equipment.Equipment;
import com.LunaLink.application.domain.model.equipment.EquipmentReservation;
import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.infrastructure.repository.equipment.EquipmentRepository;
import com.LunaLink.application.infrastructure.repository.equipment.EquipmentReservationRepository;
import com.LunaLink.application.web.dto.EquipmentDTO.EquipmentReservationRequestDTO;
import com.LunaLink.application.web.dto.EquipmentDTO.EquipmentReservationResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private EquipmentReservationRepository reservationRepository;
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
        when(reservationRepository.hasConflict(any(), any(), any(), any(), anyList())).thenReturn(false);
        
        EquipmentReservation savedReservation = new EquipmentReservation(tv, user, requestDTO.date(), requestDTO.startTime(), requestDTO.endTime());
        when(reservationRepository.save(any(EquipmentReservation.class))).thenReturn(savedReservation);

        // Act
        EquipmentReservationResponseDTO result = service.createReservation(requestDTO, "user@email.com");

        // Assert
        assertNotNull(result);
        assertEquals(EquipmentReservationStatus.CONFIRMED, result.status());
        verify(reservationRepository).save(any(EquipmentReservation.class));
    }

    @Test
    @DisplayName("Deve falhar ao criar reserva com conflito de horário")
    void createReservation_ShouldFail_WhenTimeHasConflict() {
        // Arrange
        when(userRepository.findByEmail("user@email.com")).thenReturn(user);
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(tv));
        when(reservationRepository.hasConflict(any(), any(), any(), any(), anyList())).thenReturn(true);

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
        
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(EquipmentReservation.class))).thenReturn(reservation);

        // Act
        service.handoverEquipment(reservationId);

        // Assert
        assertEquals(EquipmentReservationStatus.IN_USE, reservation.getStatus());
        assertNotNull(reservation.getPickedUpAt());
    }

    @Test
    @DisplayName("Deve devolver equipamento e mudar status para RETURNED")
    void returnEquipment_ShouldChangeStatus_ToReturned() {
        // Arrange
        UUID reservationId = UUID.randomUUID();
        EquipmentReservation reservation = new EquipmentReservation(tv, user, LocalDate.now(), LocalTime.now(), LocalTime.now().plusHours(2));
        reservation.setStatus(EquipmentReservationStatus.IN_USE);
        
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(EquipmentReservation.class))).thenReturn(reservation);

        // Act
        service.returnEquipment(reservationId);

        // Assert
        assertEquals(EquipmentReservationStatus.RETURNED, reservation.getStatus());
        assertNotNull(reservation.getReturnedAt());
    }
}
