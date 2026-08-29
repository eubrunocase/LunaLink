package com.LunaLink.application.domain.events.announcementEvents;

import java.time.LocalDateTime;
import java.util.UUID;

public class AnnouncementCreatedEvent {

    private final UUID announcementId;
    private final String Title;
    private final String Content;
    private final LocalDateTime createdAt;

    public AnnouncementCreatedEvent(UUID announcementId, String Title, String Content, LocalDateTime createdAt) {
        this.announcementId = announcementId;
        this.Title = Title;
        this.Content = Content;
        this.createdAt = createdAt;
    }

    public UUID getAnnouncementId() {
        return announcementId;
    }

    public String getTitle() {
        return Title;
    }

    public String getContent() {
        return Content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
