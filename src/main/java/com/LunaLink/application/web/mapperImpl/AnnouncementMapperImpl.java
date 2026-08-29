package com.LunaLink.application.web.mapperImpl;

import com.LunaLink.application.domain.model.announcement.Announcement;
import com.LunaLink.application.infrastructure.mapper.AnnouncementMapper;
import com.LunaLink.application.web.dto.AnnouncementDTO.RequestAnnouncementDTO;
import com.LunaLink.application.web.dto.AnnouncementDTO.ResponseAnnouncementDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AnnouncementMapperImpl implements AnnouncementMapper {
    @Override
    public ResponseAnnouncementDTO toDTO(Announcement announcement) {
        if(announcement == null) {
            return null;
        }

        return new ResponseAnnouncementDTO(announcement.getId(),
                                           announcement.getTitle(),
                                           announcement.getContent(),
                                           announcement.getCreatedAt());
    }

    @Override
    public Announcement toEntity(RequestAnnouncementDTO announcementDTO) {
        if(announcementDTO == null) {
            return null;
        }
        return new Announcement(announcementDTO.Title(),
                                announcementDTO.Content());
    }

    @Override
    public List<ResponseAnnouncementDTO> toDTOList(List<Announcement> announcements) {
        if(announcements == null) {
            return null;
        }
        return announcements.stream()
                .map(this::toDTO)
                .toList();
    }

}
