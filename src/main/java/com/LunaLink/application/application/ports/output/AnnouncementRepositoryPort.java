package com.LunaLink.application.application.ports.output;

import com.LunaLink.application.domain.model.announcement.Announcement;

import java.util.List;
import java.util.UUID;

public interface AnnouncementRepositoryPort {
    Announcement save(Announcement announcement);
    void deleteById(UUID id);
    List<Announcement> findAll();
    Announcement findAnnouncementById(UUID id);
}
