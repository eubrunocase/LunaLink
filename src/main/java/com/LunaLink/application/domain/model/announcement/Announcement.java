package com.LunaLink.application.domain.model.announcement;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table( name = "announcements")
@EqualsAndHashCode(of = "id")
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID Id;

    @JsonProperty("title")
    @Column(name = "title", nullable = false)
    private String Title;

    @JsonProperty("content")
    @Column(name = "content", nullable = false)
    private String Content;

    @JsonProperty("created_at")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime CreatedAt;

    public Announcement(String title, String content) {
        this.Title = title;
        this.Content = content;
        this.CreatedAt = LocalDateTime.now();
    }

    public Announcement() {
    }

    public UUID getId() {
        return Id;
    }

    public String getTitle() {
        return Title;
    }

    public String getContent() {
        return Content;
    }

    public LocalDateTime getCreatedAt() {
        return CreatedAt;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public void setContent(String content) {
        Content = content;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        CreatedAt = createdAt;
    }

}
