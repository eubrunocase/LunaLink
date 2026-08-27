package com.LunaLink.application.application.service.inspection;

import com.LunaLink.application.application.ports.output.InspectionRepositoryPort;
import com.LunaLink.application.application.ports.output.ReservationRepositoryPort;
import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.domain.enums.InspectionType;
import com.LunaLink.application.domain.enums.ReservationStatus;
import com.LunaLink.application.domain.enums.SpaceType;
import com.LunaLink.application.domain.enums.UserRoles;
import com.LunaLink.application.domain.events.reservationEvents.ReservationAwaitingSignatureEvent;
import com.LunaLink.application.domain.model.inspection.SpaceInspection;
import com.LunaLink.application.domain.model.reservation.Reservation;
import com.LunaLink.application.domain.model.space.Space;
import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.infrastructure.eventPublisher.EventPublisher;
import com.LunaLink.application.web.dto.ReservationsDTO.InspectionSubmitDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InspectionServiceTest {

    @Mock
    private InspectionRepositoryPort inspectionRepository;
    @Mock
    private ReservationRepositoryPort reservationRepository;
    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private EventPublisher publisher;

    @InjectMocks
    private InspectionService service;

    private Users employee;
    private Reservation reservation;
    private Space space;

    @BeforeEach
    void setUp() {
        employee = new Users("Funcionario", "101", "func@email.com", "pass", UserRoles.EMPLOYEE);
        employee.setId(UUID.randomUUID());

        space = new Space();
        space.setId(1L);
        space.setType(SpaceType.SALAO_FESTAS);

        reservation = new Reservation();
        reservation.setId(UUID.randomUUID());
        reservation.setDate(LocalDate.now().plusDays(2));
        reservation.setStatus(ReservationStatus.AWAITING_INSPECTION);
        reservation.setUser(new Users("Morador", "201", "morador@email.com", "pass", UserRoles.RESIDENT_ROLE));
        reservation.getUser().setId(UUID.randomUUID());
        reservation.setSpace(space);
    }

    private InspectionSubmitDTO buildPreEventDTO() {
        List<InspectionSubmitDTO.InspectionItemDTO> items = List.of(
                new InspectionSubmitDTO.InspectionItemDTO("Mesas", true, "http://photo/mesas.jpg"),
                new InspectionSubmitDTO.InspectionItemDTO("Cadeiras", true, "http://photo/cadeiras.jpg"),
                new InspectionSubmitDTO.InspectionItemDTO("Freezer 1", true, "http://photo/freezer1.jpg"),
                new InspectionSubmitDTO.InspectionItemDTO("Freezer 2", true, "http://photo/freezer2.jpg"),
                new InspectionSubmitDTO.InspectionItemDTO("Fogão", true, "http://photo/fogao.jpg"),
                new InspectionSubmitDTO.InspectionItemDTO("Televisão", true, "http://photo/tv.jpg")
        );
        return new InspectionSubmitDTO("Tudo OK", items);
    }

    @Test
    @DisplayName("Deve submeter vistoria pré-evento e mudar status para AWAITING_SIGNATURE")
    void submitInspection_ShouldAwaitSignature_WhenPreEvent() {
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(inspectionRepository.save(any(SpaceInspection.class))).thenAnswer(inv -> inv.getArgument(0));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        InspectionSubmitDTO dto = buildPreEventDTO();
        service.submitInspection(reservation.getId(), InspectionType.PRE_EVENT, dto, employee.getId());

        assertEquals(ReservationStatus.AWAITING_SIGNATURE, reservation.getStatus());
        verify(inspectionRepository, times(1)).save(any(SpaceInspection.class));
        verify(reservationRepository, times(1)).save(reservation);
    }

    @Test
    @DisplayName("Deve publicar ReservationAwaitingSignatureEvent após vistoria pré-evento")
    void submitInspection_ShouldPublishEvent_WhenPreEvent() {
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(inspectionRepository.save(any(SpaceInspection.class))).thenAnswer(inv -> inv.getArgument(0));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        InspectionSubmitDTO dto = buildPreEventDTO();
        service.submitInspection(reservation.getId(), InspectionType.PRE_EVENT, dto, employee.getId());

        ArgumentCaptor<ReservationAwaitingSignatureEvent> captor = ArgumentCaptor.forClass(ReservationAwaitingSignatureEvent.class);
        verify(publisher, times(1)).publishEvent(captor.capture());
        assertEquals(reservation.getId(), captor.getValue().getReservationId());
        assertEquals(reservation.getUser().getId(), captor.getValue().getUserId());
    }

    @Test
    @DisplayName("Deve submeter vistoria pós-evento sem alterar status")
    void submitInspection_ShouldNotChangeStatus_WhenPostEvent() {
        reservation.setStatus(ReservationStatus.CONFIRMED);
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(inspectionRepository.save(any(SpaceInspection.class))).thenAnswer(inv -> inv.getArgument(0));

        InspectionSubmitDTO dto = buildPreEventDTO();
        service.submitInspection(reservation.getId(), InspectionType.POST_EVENT, dto, employee.getId());

        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
        verify(reservationRepository, never()).save(any(Reservation.class));
        verify(publisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando reserva não está aguardando vistoria pré-evento")
    void submitInspection_ShouldThrow_WhenNotAwaitingInspection() {
        reservation.setStatus(ReservationStatus.CONFIRMED);
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        InspectionSubmitDTO dto = buildPreEventDTO();
        assertThrows(IllegalStateException.class,
                () -> service.submitInspection(reservation.getId(), InspectionType.PRE_EVENT, dto, employee.getId()));
    }

    @Test
    @DisplayName("Deve lançar exceção quando reserva não está CONFIRMADA para vistoria pós-evento")
    void submitInspection_ShouldThrow_WhenNotConfirmedForPostEvent() {
        reservation.setStatus(ReservationStatus.AWAITING_INSPECTION);
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        InspectionSubmitDTO dto = buildPreEventDTO();
        assertThrows(IllegalStateException.class,
                () -> service.submitInspection(reservation.getId(), InspectionType.POST_EVENT, dto, employee.getId()));
    }

    @Test
    @DisplayName("Deve lançar exceção quando reserva não é encontrada")
    void submitInspection_ShouldThrow_WhenReservationNotFound() {
        when(reservationRepository.findById(any())).thenReturn(Optional.empty());

        InspectionSubmitDTO dto = buildPreEventDTO();
        assertThrows(IllegalArgumentException.class,
                () -> service.submitInspection(UUID.randomUUID(), InspectionType.PRE_EVENT, dto, employee.getId()));
    }

    @Test
    @DisplayName("Deve lançar exceção quando quantidade de itens é inválida")
    void submitInspection_ShouldThrow_WhenInvalidItemCount() {
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));

        List<InspectionSubmitDTO.InspectionItemDTO> items = List.of(
                new InspectionSubmitDTO.InspectionItemDTO("Mesas", true, "http://photo/mesas.jpg")
        );
        InspectionSubmitDTO dto = new InspectionSubmitDTO("OK", items);

        assertThrows(IllegalArgumentException.class,
                () -> service.submitInspection(reservation.getId(), InspectionType.PRE_EVENT, dto, employee.getId()));
    }

    @Test
    @DisplayName("Deve lançar exceção quando foto está ausente")
    void submitInspection_ShouldThrow_WhenPhotoMissing() {
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));

        List<InspectionSubmitDTO.InspectionItemDTO> items = List.of(
                new InspectionSubmitDTO.InspectionItemDTO("Mesas", true, ""),
                new InspectionSubmitDTO.InspectionItemDTO("Cadeiras", true, "http://photo/cadeiras.jpg"),
                new InspectionSubmitDTO.InspectionItemDTO("Freezer 1", true, "http://photo/freezer1.jpg"),
                new InspectionSubmitDTO.InspectionItemDTO("Freezer 2", true, "http://photo/freezer2.jpg"),
                new InspectionSubmitDTO.InspectionItemDTO("Fogão", true, "http://photo/fogao.jpg"),
                new InspectionSubmitDTO.InspectionItemDTO("Televisão", true, "http://photo/tv.jpg")
        );
        InspectionSubmitDTO dto = new InspectionSubmitDTO("OK", items);

        assertThrows(IllegalArgumentException.class,
                () -> service.submitInspection(reservation.getId(), InspectionType.PRE_EVENT, dto, employee.getId()));
    }
}
