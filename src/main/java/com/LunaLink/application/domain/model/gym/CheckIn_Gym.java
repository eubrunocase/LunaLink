package com.LunaLink.application.domain.model.gym;

import com.LunaLink.application.domain.model.resident.Resident;
import com.LunaLink.application.domain.model.space.Space;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "CheckIn_gym")
public class CheckIn_Gym {

    @Id
    @JsonProperty("id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id", nullable = false)
    private Resident resident;

    @JsonProperty("entrada")
    private LocalDateTime entrada;

    @JsonProperty("space")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "space_id")
    private Space space;

    public void assignTo(Resident resident, Space space) {
        this.resident = resident;
        this.space = space;
    }

    public CheckIn_Gym() {
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getEntrada() {
        return entrada;
    }

    public Resident getResident() {
        return resident;
    }

    public void setEntrada(LocalDateTime entrada) {
        this.entrada = entrada;
    }

    public void setResident(Resident resident) {
        this.resident = resident;
    }

    public Space getSpace() {
        return space;
    }

}
