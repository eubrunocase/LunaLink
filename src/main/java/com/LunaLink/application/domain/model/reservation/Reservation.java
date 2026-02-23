package com.LunaLink.application.domain.model.reservation;

import com.LunaLink.application.domain.enums.ReservationStatus;
import com.LunaLink.application.domain.model.space.Space;
import com.LunaLink.application.domain.model.users.Users;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "reservation",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_date_space",
                        columnNames = {"user_id", "date", "space_id"})
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "space_id")
    private Space space;

    @JsonProperty("status")
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private ReservationStatus status;

    @JsonProperty("createdAt")
    @Column(name = "createdAt", nullable = true)
    private LocalDateTime createdAt;

    @JsonProperty("canceledAt")
    @Column(name = "canceledAt", nullable = true)
    private LocalDateTime canceledAt;

    public void assignTo(Users users, Space space) {
        this.user = users;
        this.space = space;
    }

    public UUID getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public Space getSpace() {
        return space;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setSpace(Space space) {
        this.space = space;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users users) {
        this.user = users;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCanceledAt() {
        return canceledAt;
    }

    public void setCanceledAt(LocalDateTime canceledAt) {
        this.canceledAt = canceledAt;
    }
}