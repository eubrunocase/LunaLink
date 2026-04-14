package com.LunaLink.application.domain.events.occurrenceEvents;

import java.util.UUID;

public class OccurrenceCreatedEvent {

    private final UUID occurrenceId;
    private final String userName;
    private final String descriptionSnippet;

    public OccurrenceCreatedEvent(UUID occurrenceId, String userName, String descriptionSnippet) {
        this.occurrenceId = occurrenceId;
        this.userName = userName;
        this.descriptionSnippet = descriptionSnippet;
    }

    public UUID getOccurrenceId() {
        return occurrenceId;
    }

    public String getUserName() {
        return userName;
    }

    public String getDescriptionSnippet() {
        return descriptionSnippet;
    }
}
