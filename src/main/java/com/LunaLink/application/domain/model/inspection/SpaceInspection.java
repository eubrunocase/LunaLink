package com.LunaLink.application.domain.model.inspection;

import com.LunaLink.application.domain.enums.InspectionType;
import com.LunaLink.application.domain.model.reservation.Reservation;
import com.LunaLink.application.domain.model.users.Users;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "space_inspection")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SpaceInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @JsonProperty("type")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InspectionType type;

    @JsonProperty("notes")
    @Column(columnDefinition = "TEXT", nullable = true)
    private String notes;

    @JsonProperty("inspectedAt")
    @Column(nullable = false)
    private LocalDateTime inspectedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Users employee;

    @OneToMany(mappedBy = "inspection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SpaceInspectionItem> items = new ArrayList<>();

    public SpaceInspection(InspectionType type, String notes, Reservation reservation, Users employee) {
        this.type = type;
        this.notes = notes;
        this.reservation = reservation;
        this.employee = employee;
        this.inspectedAt = LocalDateTime.now();
    }

    public void addItem(SpaceInspectionItem item) {
        items.add(item);
        item.setInspection(this);
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }
}
