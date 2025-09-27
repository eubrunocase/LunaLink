package com.LunaLink.application.core.domain.gym;

import com.LunaLink.application.core.domain.Reservation;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "checkOut_gym")
public class CheckOut_Gym {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonProperty("id")
    private UUID id;

    @JsonProperty("gymReservation")
    @OneToOne(fetch = FetchType.LAZY)
    private CheckIn_Gym checkInGym;

    @JsonProperty("saida")
    private LocalDateTime saida;


    public void AssignTo(CheckIn_Gym checkInGym) {
        this.checkInGym = checkInGym;
    }

    public UUID getId() {
        return id;
    }

    public CheckIn_Gym getCheckInGym() {
        return checkInGym;
    }

    public LocalDateTime getSaida() {
        return saida;
    }

    public void setSaida(LocalDateTime saida) {
        this.saida = saida;
    }

    public void setCheckInGym(CheckIn_Gym checkInGym) {
        this.checkInGym = checkInGym;
    }


}
