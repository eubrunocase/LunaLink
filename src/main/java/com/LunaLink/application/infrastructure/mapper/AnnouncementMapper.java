package com.LunaLink.application.infrastructure.mapper;

import com.LunaLink.application.domain.model.announcement.Announcement;
import com.LunaLink.application.web.dto.AnnouncementDTO.RequestAnnouncementDTO;
import com.LunaLink.application.web.dto.AnnouncementDTO.ResponseAnnouncementDTO;

import java.util.List;

public interface AnnouncementMapper {
    ResponseAnnouncementDTO toDTO(Announcement announcement);
    Announcement toEntity(RequestAnnouncementDTO announcementDTO);
    List<ResponseAnnouncementDTO> toDTOList(List<Announcement> announcements);
}
