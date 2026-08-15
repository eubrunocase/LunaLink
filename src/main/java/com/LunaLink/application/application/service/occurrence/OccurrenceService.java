package com.LunaLink.application.application.service.occurrence;

import com.LunaLink.application.application.ports.input.OccurrenceServicePort;
import com.LunaLink.application.application.ports.output.OccurrenceRepositoryPort;
import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.domain.enums.UserRoles;
import com.LunaLink.application.domain.events.occurrenceEvents.OccurrenceCreatedEvent;
import com.LunaLink.application.domain.model.occurrence.Occurrence;
import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.infrastructure.eventPublisher.EventPublisher;
import com.LunaLink.application.infrastructure.mapper.OccurrenceMapper;
import com.LunaLink.application.web.dto.OccurrenceDTO.OccurrenceCreateRequestDTO;
import com.LunaLink.application.web.dto.OccurrenceDTO.OccurrenceResponseDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OccurrenceService implements OccurrenceServicePort {

    private final OccurrenceRepositoryPort occurrenceRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final EventPublisher eventPublisher;
    private final OccurrenceMapper mapper;

    public OccurrenceService(OccurrenceRepositoryPort occurrenceRepositoryPort,
                             UserRepositoryPort userRepositoryPort,
                             EventPublisher eventPublisher,
                             OccurrenceMapper mapper) {
        this.occurrenceRepositoryPort = occurrenceRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.eventPublisher = eventPublisher;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public OccurrenceResponseDTO createOccurrence(OccurrenceCreateRequestDTO dto, String userEmail) {
        if (dto.incidentDate().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("A data do incidente não pode ser no futuro.");
        }

        Users user = userRepositoryPort.findByEmail(userEmail);
        if (user == null) {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }

        Occurrence occurrence = new Occurrence(user, dto.description(), dto.incidentDate());
        Occurrence savedOccurrence = occurrenceRepositoryPort.save(occurrence);

        String snippet = dto.description().length() > 50 
                ? dto.description().substring(0, 50) + "..." 
                : dto.description();
                
        OccurrenceCreatedEvent event = new OccurrenceCreatedEvent(
                savedOccurrence.getId(),
                user.getName(),
                snippet
        );
        eventPublisher.publishEvent(event);

        return new OccurrenceResponseDTO(
                savedOccurrence.getId(),
                user.getName(),
                savedOccurrence.getDescription(),
                savedOccurrence.getIncidentDate(),
                savedOccurrence.getCreatedAt()
        );
    }

    @Override
    public void deleteOccurrence(UUID uuid, String userEmail) {
        Users requester = userRepositoryPort.findByEmail(userEmail);
        Optional<Occurrence> o = occurrenceRepositoryPort.findById(uuid);
        if (o.isEmpty()) {
            return;
        }
        if (requester != null && requester.getRole() != UserRoles.ADMIN_ROLE
                && !o.get().getUser().getId().equals(requester.getId())) {
            throw new IllegalArgumentException("Você não pode excluir uma ocorrência de outro morador.");
        }
        occurrenceRepositoryPort.deleteById(uuid);
    }

    @Override
    public OccurrenceResponseDTO findById(UUID uuid, String userEmail) {
        Users requester = userRepositoryPort.findByEmail(userEmail);
        Optional<Occurrence> o = occurrenceRepositoryPort.findById(uuid);
        if (o.isEmpty()) {
            throw new IllegalArgumentException("Ocorrência não encontrada.");
        }
        if (requester == null || (requester.getRole() != UserRoles.ADMIN_ROLE
                && !o.get().getUser().getId().equals(requester.getId()))) {
            throw new IllegalArgumentException("Ocorrência não encontrada.");
        }
        return mapper.toDto(o.get());
    }

    @Override
    public List<OccurrenceResponseDTO> findAll(String userEmail) {
        Users requester = userRepositoryPort.findByEmail(userEmail);
        List<Occurrence> occurrences;
        if (requester != null && requester.getRole() == UserRoles.ADMIN_ROLE) {
            occurrences = occurrenceRepositoryPort.findAll();
        } else {
            occurrences = occurrenceRepositoryPort.findAll().stream()
                    .filter(o -> requester != null && o.getUser().getId().equals(requester.getId()))
                    .toList();
        }
        return occurrences.stream()
                .map(mapper::toDto)
                .toList();
    }
}
