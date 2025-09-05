package com.LunaLink.application.core.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(
        name = "reservation",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_resident_date",
                        columnNames = {"resident_id", "date", "space_id"})
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resident_id")
    private Resident resident;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "space_id")
    private Space space;

    public void assignTo(Resident resident, Space space) {
        this.resident = resident;
        this.space = space;
    }

    public long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public Resident getResident() {
        return resident;
    }

    public Space getSpace() {
        return space;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setResident(Resident resident) {
        this.resident = resident;
    }

    public void setSpace(Space space) {
        this.space = space;
    }
}