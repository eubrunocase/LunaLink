package com.LunaLink.application.application.service.inspection;

import com.LunaLink.application.application.ports.input.InspectionServicePort;
import com.LunaLink.application.application.ports.input.StorageService;
import com.LunaLink.application.application.ports.output.InspectionRepositoryPort;
import com.LunaLink.application.application.ports.output.ReservationRepositoryPort;
import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.domain.enums.InspectionType;
import com.LunaLink.application.domain.enums.ReservationStatus;
import com.LunaLink.application.domain.events.reservationEvents.ReservationAwaitingSignatureEvent;
import com.LunaLink.application.domain.model.inspection.SpaceInspection;
import com.LunaLink.application.domain.model.inspection.SpaceInspectionItem;
import com.LunaLink.application.domain.model.reservation.Reservation;
import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.infrastructure.config.SpaceEquipmentCatalog;
import com.LunaLink.application.infrastructure.eventPublisher.EventPublisher;
import com.LunaLink.application.web.dto.ReservationsDTO.InspectionSubmitDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class InspectionService implements InspectionServicePort {

    private final InspectionRepositoryPort inspectionRepository;
    private final ReservationRepositoryPort reservationRepository;
    private final UserRepositoryPort userRepository;
    private final EventPublisher publisher;
    private final StorageService storageService;

    @Value("15")
    private long uploadExpirationMinutes;

    @Value("15")
    private long downloadExpirationMinutes;

    public InspectionService(InspectionRepositoryPort inspectionRepository,
                             ReservationRepositoryPort reservationRepository,
                             UserRepositoryPort userRepository,
                             EventPublisher publisher,
                             StorageService storageService) {
        this.inspectionRepository = inspectionRepository;
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.publisher = publisher;
        this.storageService = storageService;
    }

    @Transactional
    @Override
    public void submitInspection(UUID reservationId, InspectionType type, InspectionSubmitDTO dto, UUID employeeId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva não encontrada."));

        if (type == InspectionType.PRE_EVENT
                && reservation.getStatus() != ReservationStatus.AWAITING_INSPECTION) {
            throw new IllegalStateException("Reserva não está aguardando vistoria pré-evento.");
        }

        if (type == InspectionType.POST_EVENT
                && reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Reserva não está confirmada para realizar vistoria pós-evento.");
        }

        Users employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado."));

        validateInspectionItems(reservation, dto);

        SpaceInspection inspection = new SpaceInspection(type, dto.notes(), reservation, employee);

        for (InspectionSubmitDTO.InspectionItemDTO itemDto : dto.items()) {
            SpaceInspectionItem item = new SpaceInspectionItem(
                    itemDto.equipmentName(),
                    itemDto.okConfirmed(),
                    itemDto.voucherKey()
            );
            inspection.addItem(item);
        }

        inspectionRepository.save(inspection);

        if (type == InspectionType.PRE_EVENT) {
            reservation.setStatus(ReservationStatus.AWAITING_SIGNATURE);
            reservationRepository.save(reservation);

            ReservationAwaitingSignatureEvent event = new ReservationAwaitingSignatureEvent(
                    reservation.getId(),
                    reservation.getUser().getId(),
                    reservation.getDate(),
                    reservation.getSpace()
            );
            publisher.publishEvent(event);
        }
    }

    private void validateInspectionItems(Reservation reservation, InspectionSubmitDTO dto) {
        List<String> expectedEquipment = SpaceEquipmentCatalog.getEquipmentForSpace(reservation.getSpace().getType());

        if (expectedEquipment.isEmpty()) {
            throw new IllegalStateException("Espaço não possui equipamentos para vistoria.");
        }

        if (dto.items().size() != expectedEquipment.size()) {
            throw new IllegalArgumentException(
                    String.format("Quantidade de itens inválida. Esperado %d, informado %d.",
                            expectedEquipment.size(), dto.items().size()));
        }

        for (String equipment : expectedEquipment) {
            boolean found = dto.items().stream()
                    .anyMatch(item -> item.equipmentName().equals(equipment));
            if (!found) {
                throw new IllegalArgumentException(
                        String.format("Equipamento obrigatório não informado: %s", equipment));
            }
        }

        for (InspectionSubmitDTO.InspectionItemDTO item : dto.items()) {
            if (item.voucherKey() == null || item.voucherKey().isBlank()) {
                throw new IllegalArgumentException(
                        String.format("Foto obrigatória não informada para o equipamento: %s", item.equipmentName()));
            }
        }
    }

    @Override
    public Map<String, String> generateUploadData(UUID userId, String fileName) {
        String key = "vistorias/" + userId + "/" + UUID.randomUUID() + "-" + fileName;
        Duration expiration = Duration.ofMinutes(uploadExpirationMinutes);
        String uploadUrl = storageService.generateUploadUrl(key, expiration);
        return Map.of("uploadUrl", uploadUrl, "key", key);
    }

    @Override
    public List<Map<String, String>> generateDownloadUrls(UUID inspectionId) {
        SpaceInspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new IllegalArgumentException("Inspeção não encontrada."));

        Duration expiration = Duration.ofMinutes(downloadExpirationMinutes);

        return inspection.getItems().stream()
                .map(item -> Map.of(
                        "itemId", item.getId().toString(),
                        "downloadUrl", storageService.generateDownloadUrl(item.getVoucherKey(), expiration)
                ))
                .toList();
    }

    @Override
    public String generateDownloadUrl(UUID inspectionId, UUID itemId) {
        SpaceInspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new IllegalArgumentException("Inspeção não encontrada."));

        SpaceInspectionItem item = inspection.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item de vistoria não encontrado."));

        Duration expiration = Duration.ofMinutes(downloadExpirationMinutes);
        return storageService.generateDownloadUrl(item.getVoucherKey(), expiration);
    }
}
