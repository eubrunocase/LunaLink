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
@Table(name = "liability_term")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LiabilityTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @JsonProperty("content")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @JsonProperty("signedByResident")
    @Column(nullable = false)
    private boolean signedByResident = false;

    @JsonProperty("signedAt")
    @Column(nullable = true)
    private LocalDateTime signedAt;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    private Reservation reservation;

    public LiabilityTerm(String content, Reservation reservation) {
        this.content = content;
        this.reservation = reservation;
        this.signedByResident = false;
    }

    public void sign() {
        this.signedByResident = true;
        this.signedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public String getContent() { return content; }
    public boolean isSignedByResident() { return signedByResident; }
    public LocalDateTime getSignedAt() { return signedAt; }
    public Reservation getReservation() { return reservation; }
}
