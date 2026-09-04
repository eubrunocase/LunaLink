package com.LunaLink.application.domain.model.reservation;

import com.LunaLink.application.domain.enums.ReservationStatus;
import com.LunaLink.application.domain.model.inspection.SpaceInspection;
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
import java.util.ArrayList;
import java.util.List;
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

    @JsonProperty("notes")
    @Column(columnDefinition = "TEXT", nullable = true)
    private String notes;

    @JsonProperty("createdAt")
    @Column(nullable = true)
    private LocalDateTime createdAt;

    @JsonProperty("canceledAt")
    @Column(nullable = true)
    private LocalDateTime canceledAt;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Guest> guestList = new ArrayList<>();

    @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private LiabilityTerm liabilityTerm;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SpaceInspection> inspections = new ArrayList<>();

    public void assignTo(Users users, Space space) {
        this.user = users;
        this.space = space;
    }

    public void addGuest(Guest guest) {
        guestList.add(guest);
        guest.setReservation(this);
    }

    public void addInspection(SpaceInspection inspection) {
        inspections.add(inspection);
        inspection.setReservation(this);
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public List<Guest> getGuestList() {
        return guestList;
    }

    public void setGuestList(List<Guest> guestList) {
        this.guestList = guestList;
    }

    public LiabilityTerm getLiabilityTerm() {
        return liabilityTerm;
    }

    public void setLiabilityTerm(LiabilityTerm liabilityTerm) {
        this.liabilityTerm = liabilityTerm;
    }

    public List<SpaceInspection> getInspections() {
        return inspections;
    }

    public void setInspections(List<SpaceInspection> inspections) {
        this.inspections = inspections;
    }
}
