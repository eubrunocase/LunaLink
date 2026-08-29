package com.LunaLink.application.infrastructure.repository.announcement;

import com.LunaLink.application.application.ports.output.AnnouncementRepositoryPort;
import com.LunaLink.application.domain.model.announcement.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, UUID>, AnnouncementRepositoryPort {
}
