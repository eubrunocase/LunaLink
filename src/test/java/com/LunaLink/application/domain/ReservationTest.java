package com.LunaLink.application.domain;

import com.LunaLink.application.domain.enums.SpaceType;
import com.LunaLink.application.domain.model.reservation.Reservation;
import com.LunaLink.application.domain.model.resident.Resident;
import com.LunaLink.application.domain.model.space.Space;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("Reservation Domain Tests")
public class ReservationTest {

    private Reservation reservation;
    private Resident resident;
    private Space space;

    @BeforeEach
    void setUp() {
        reservation = new Reservation();

        resident.setId(1L);
        resident.setLogin("test");
        resident.setPassword("senha");

        space = new Space();
        space.setType(SpaceType.CHURRASQUEIRA);
    }

    @Test
    @DisplayName("Criar Reserva Com Construtor Vazio")
    void CriarReservaComConstrutorVazio () {
        Reservation reservation = new Reservation();

        assertEquals(1, reservation.getId());
        assertNull(reservation.getDate());
        assertNull(reservation.getResident());
        assertNull(reservation.getSpace());
    }

    @Test
    @DisplayName("Criar Reserva Com Construtor Completo")
    void CriarReservaComConstrutorCompleto () {

        long id = 1;
        LocalDate date = LocalDate.now();

        Reservation reservation = new Reservation(id, date, resident, space);

        assertEquals(id, reservation.getId());
        assertEquals(date, reservation.getDate());
        assertEquals(resident, reservation.getResident());
        assertEquals(space, reservation.getSpace());
    }




}
