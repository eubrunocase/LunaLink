package com.LunaLink.application.application.facades.announcement;

import com.LunaLink.application.application.service.announcement.AnnouncementService;
import com.LunaLink.application.web.dto.AnnouncementDTO.RequestAnnouncementDTO;
import com.LunaLink.application.web.dto.AnnouncementDTO.ResponseAnnouncementDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class AnnouncementFacade {

    private final AnnouncementService announcementService;

    public AnnouncementFacade(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    public ResponseAnnouncementDTO createAnnouncement(RequestAnnouncementDTO dto) {
        return announcementService.createAnnouncement(dto);
    }

    public ResponseAnnouncementDTO findAnnouncementById(UUID id) {
        return announcementService.findAnnouncementById(id);
    }

    public void deleteAnnouncement(UUID id) {
        announcementService.deleteAnnouncement(id);
    }

    public List<ResponseAnnouncementDTO> findAllAnnouncements() {
        return announcementService.findAllAnnouncements();
    }

    public ResponseAnnouncementDTO updateAnnouncement(UUID id, RequestAnnouncementDTO dto) {
        return announcementService.updateAnnouncement(id, dto);
    }

}
