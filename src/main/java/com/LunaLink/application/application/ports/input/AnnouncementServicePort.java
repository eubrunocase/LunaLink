package com.LunaLink.application.application.ports.input;

import com.LunaLink.application.web.dto.AnnouncementDTO.RequestAnnouncementDTO;
import com.LunaLink.application.web.dto.AnnouncementDTO.ResponseAnnouncementDTO;

import java.util.List;
import java.util.UUID;

public interface AnnouncementServicePort {

    ResponseAnnouncementDTO createAnnouncement(RequestAnnouncementDTO dto);
    ResponseAnnouncementDTO findAnnouncementById(UUID id);
    List<ResponseAnnouncementDTO> findAllAnnouncements();
    void deleteAnnouncement(UUID id);
    ResponseAnnouncementDTO updateAnnouncement(UUID id, RequestAnnouncementDTO dto);
}
