package com.LunaLink.application.application.service.equipment;

import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.domain.enums.EquipmentReservationStatus;
import com.LunaLink.application.domain.model.equipment.Equipment;
import com.LunaLink.application.domain.model.equipment.EquipmentReservation;
import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.infrastructure.repository.equipment.EquipmentRepository;
import com.LunaLink.application.infrastructure.repository.equipment.EquipmentReservationRepository;
import com.LunaLink.application.web.dto.EquipmentDTO.EquipmentReservationRequestDTO;
import com.LunaLink.application.web.dto.EquipmentDTO.EquipmentReservationResponseDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EquipmentReservationService {

    private final EquipmentReservationRepository reservationRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepositoryPort userRepository;

    private static final List<EquipmentReservationStatus> ACTIVE_STATUSES = List.of(
            EquipmentReservationStatus.CONFIRMED,
            EquipmentReservationStatus.IN_USE
    );

    public EquipmentReservationService(EquipmentReservationRepository reservationRepository,
                                       EquipmentRepository equipmentRepository,
                                       UserRepositoryPort userRepository) {
        this.reservationRepository = reservationRepository;
        this.equipmentRepository = equipmentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public EquipmentReservationResponseDTO createReservation(EquipmentReservationRequestDTO dto, String userEmail) {
        // 1. Validações básicas
        if (dto.date().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("A data da reserva não pode ser no passado.");
        }
        if (!dto.endTime().isAfter(dto.startTime())) {
            throw new IllegalArgumentException("O horário de término deve ser após o início.");
        }

        Users user = userRepository.findByEmail(userEmail);
        Equipment equipment = equipmentRepository.findById(dto.equipmentId())
                .orElseThrow(() -> new IllegalArgumentException("Equipamento não encontrado."));

        if (!equipment.isActive()) {
            throw new IllegalStateException("Equipamento está inativo ou em manutenção.");
        }

        // 2. Validação de Conflito de Horário
        boolean hasConflict = reservationRepository.hasConflict(
                dto.equipmentId(),
                dto.date(),
                dto.startTime(),
                dto.endTime(),
                ACTIVE_STATUSES
        );

        if (hasConflict) {
            throw new IllegalStateException("O equipamento já está reservado neste horário.");
        }

        // 3. Criação e Auto-aprovação
        EquipmentReservation reservation = new EquipmentReservation(
                equipment, user, dto.date(), dto.startTime(), dto.endTime()
        );
        
        EquipmentReservation saved = reservationRepository.save(reservation);
        return convertToDTO(saved);
    }

    @Transactional
    public EquipmentReservationResponseDTO handoverEquipment(UUID id) {
        EquipmentReservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva não encontrada."));

        if (reservation.getStatus() != EquipmentReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Apenas reservas CONFIRMED podem ser retiradas. Status atual: " + reservation.getStatus());
        }

        reservation.setStatus(EquipmentReservationStatus.IN_USE);
        reservation.setPickedUpAt(LocalDateTime.now());
        
        return convertToDTO(reservationRepository.save(reservation));
    }

    @Transactional
    public EquipmentReservationResponseDTO returnEquipment(UUID id) {
        EquipmentReservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva não encontrada."));

        if (reservation.getStatus() != EquipmentReservationStatus.IN_USE) {
            throw new IllegalStateException("Apenas reservas IN_USE podem ser devolvidas. Status atual: " + reservation.getStatus());
        }

        reservation.setStatus(EquipmentReservationStatus.RETURNED);
        reservation.setReturnedAt(LocalDateTime.now());

        return convertToDTO(reservationRepository.save(reservation));
    }

    public List<EquipmentReservationResponseDTO> listReservations(LocalDate date, EquipmentReservationStatus status) {
        List<EquipmentReservation> reservations;

        if (date != null && status != null) {
            reservations = reservationRepository.findAllByDateAndStatus(date, status);
        } else if (date != null) {
            reservations = reservationRepository.findAllByDate(date);
        } else if (status != null) {
            reservations = reservationRepository.findAllByStatus(status);
        } else {
            reservations = reservationRepository.findAll();
        }

        return reservations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private EquipmentReservationResponseDTO convertToDTO(EquipmentReservation r) {
        return new EquipmentReservationResponseDTO(
                r.getId(),
                r.getEquipment().getName(),
                r.getUser().getName(),
                r.getUser().getApartment(),
                r.getDate(),
                r.getStartTime(),
                r.getEndTime(),
                r.getStatus(),
                r.getCreatedAt(),
                r.getPickedUpAt(),
                r.getReturnedAt()
        );
    }
}
