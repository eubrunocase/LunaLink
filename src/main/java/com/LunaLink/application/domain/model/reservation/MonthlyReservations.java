package com.LunaLink.application.domain.model.reservation;

import com.LunaLink.application.domain.model.resident.Resident;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jdk.jfr.Timestamp;

import java.time.LocalDateTime;

@Entity
public class MonthlyReservations {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "resident_id",
            nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_monthly_reservations_resident")
    )
    private Resident resident;

    @Timestamp
    private LocalDateTime creationDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "reservation_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_monthly_reservations_space")
    )
    private Reservation reservation;

    public MonthlyReservations() {
    }

    public MonthlyReservations(Resident resident, Reservation reservation) {
        this.resident = resident;
        this.creationDate = LocalDateTime.now();
        this.reservation = reservation;
    }

    public long getId() {
        return id;
    }

    public Resident getResident() {
        return resident;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setResident(Resident resident) {
        this.resident = resident;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }
}
