package com.LunaLink.application.core;

import com.LunaLink.application.core.enums.SpaceType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "space")
@AllArgsConstructor
@NoArgsConstructor
public class Space {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @JsonProperty("type")
    private SpaceType type;

    @OneToMany(mappedBy = "space", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations = new ArrayList<>();


    public List<Reservation> getReservations() {
        return reservations;
    }

    public SpaceType getType() {
        return type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setType(SpaceType type) {
        this.type = type;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }
}
