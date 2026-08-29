package com.LunaLink.application.application.service.announcement;

import com.LunaLink.application.application.ports.input.AnnouncementServicePort;
import com.LunaLink.application.application.ports.output.AnnouncementRepositoryPort;
import com.LunaLink.application.domain.events.announcementEvents.AnnouncementCreatedEvent;
import com.LunaLink.application.domain.model.announcement.Announcement;
import com.LunaLink.application.infrastructure.eventPublisher.EventPublisher;
import com.LunaLink.application.infrastructure.mapper.AnnouncementMapper;
import com.LunaLink.application.web.dto.AnnouncementDTO.RequestAnnouncementDTO;
import com.LunaLink.application.web.dto.AnnouncementDTO.ResponseAnnouncementDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AnnouncementService implements AnnouncementServicePort {

    private final AnnouncementRepositoryPort repository;
    private final AnnouncementMapper mapper;
    private final EventPublisher publisher;

    public AnnouncementService(AnnouncementRepositoryPort repository, AnnouncementMapper mapper, EventPublisher publisher) {
        this.repository = repository;
        this.mapper = mapper;
        this.publisher = publisher;
    }

    @Override
    @Transactional
    public ResponseAnnouncementDTO createAnnouncement(RequestAnnouncementDTO requestAnnouncementDTO) {
        if(requestAnnouncementDTO == null) {
            throw new RuntimeException("MÉTODO CREATE Announcement DE AnnouncementService: Announcement não pode ser nulo.");
        }

        Announcement announcement = mapper.toEntity(requestAnnouncementDTO);
        Announcement savedAnnouncement = repository.save(announcement);

        AnnouncementCreatedEvent event = new AnnouncementCreatedEvent(
                savedAnnouncement.getId(),
                savedAnnouncement.getTitle(),
                savedAnnouncement.getContent(),
                savedAnnouncement.getCreatedAt()
        );
        publisher.publishEvent(event);

        return mapper.toDTO(savedAnnouncement);
    }

    @Override
    public ResponseAnnouncementDTO findAnnouncementById(UUID id) {
        if(id == null) throw new RuntimeException("MÉTODO findAnnouncementById de AnnouncementService: Announcement não pode ser nulo.");
        Announcement a = repository.findAnnouncementById(id);
        return mapper.toDTO(a);
    }

    @Override
    public List<ResponseAnnouncementDTO> findAllAnnouncements() {
        List<Announcement> announcements = repository.findAll();
        return mapper.toDTOList(announcements);
    }

    @Override
    @Transactional
    public void deleteAnnouncement(UUID id) {
        if(id == null) throw new RuntimeException("MÉTODO deleteAnnouncement de AnnouncementService: Announcement não pode ser nulo.");
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public ResponseAnnouncementDTO updateAnnouncement(UUID id, RequestAnnouncementDTO dto) {
        if(id == null) throw new RuntimeException("MÉTODO updateAnnouncement de AnnouncementService: Announcement não pode ser nulo.");
        Announcement announcementForUpdate = repository.findAnnouncementById(id);
        announcementForUpdate.setTitle(dto.Title());
        announcementForUpdate.setContent(dto.Content());
        return mapper.toDTO(repository.save(announcementForUpdate));
    }
}
