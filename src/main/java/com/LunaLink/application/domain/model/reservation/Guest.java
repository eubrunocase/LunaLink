package com.LunaLink.application.domain.model.reservation;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "guest")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @JsonProperty("name")
    @Column(nullable = false)
    private String name;

    @JsonProperty("checkedIn")
    @Column(nullable = false)
    private boolean checkedIn = false;

    @JsonProperty("checkedInAt")
    @Column(nullable = true)
    private LocalDateTime checkedInAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    public Guest(String name, Reservation reservation) {
        this.name = name;
        this.reservation = reservation;
        this.checkedIn = false;
    }

    public void checkIn() {
        this.checkedIn = true;
        this.checkedInAt = LocalDateTime.now();
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public boolean isCheckedIn() { return checkedIn; }
    public LocalDateTime getCheckedInAt() { return checkedInAt; }
    public Reservation getReservation() { return reservation; }
}
