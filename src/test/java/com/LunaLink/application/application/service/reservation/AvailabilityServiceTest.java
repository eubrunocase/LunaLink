package com.LunaLink.application.application.service.reservation;

import com.LunaLink.application.application.ports.output.ReservationRepositoryPort;
import com.LunaLink.application.web.dto.ReservationsDTO.availabilityReservations.AvailabilityResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private ReservationRepositoryPort reservationRepository;

    @InjectMocks
    private AvailabilityService service;

    @Test
    @DisplayName("Deve retornar datas indisponíveis corretamente")
    void getUnavailableDates_ShouldReturnDates_WhenFound() {
        // Arrange
        Long spaceId = 1L;
        Integer year = 2026;
        Integer month = 1;
        List<LocalDate> unavailableDates = List.of(LocalDate.of(2026, 1, 1));

        when(reservationRepository.findUnavailableDatesBySpaceAndMonth(spaceId, year, month))
                .thenReturn(unavailableDates);

        // Act
        List<LocalDate> result = service.getUnavailableDates(spaceId, year, month);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(LocalDate.of(2026, 1, 1), result.get(0));
    }

    @Test
    @DisplayName("Deve verificar disponibilidade de data corretamente")
    void isDateAvailable_ShouldReturnTrue_WhenNoReservation() {
        // Arrange
        Long spaceId = 1L;
        LocalDate date = LocalDate.now().plusDays(1);

        when(reservationRepository.existsBySpaceAndDate(spaceId, date)).thenReturn(false);

        // Act
        boolean result = service.isDateAvailable(spaceId, date);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Deve verificar disponibilidade em período corretamente")
    void checkPeriodAvailability_ShouldReturnCorrectDTO() {
        // Arrange
        Long spaceId = 1L;
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(5);
        List<LocalDate> unavailableDates = List.of(startDate.plusDays(1));

        when(reservationRepository.findUnavailableDatesBySpaceAndPeriod(spaceId, startDate, endDate))
                .thenReturn(unavailableDates);

        // Act
        AvailabilityService.PeriodAvailabilityDTO result = service.checkPeriodAvailability(spaceId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(spaceId, result.getSpaceId());
        assertEquals(startDate, result.getStartDate());
        assertEquals(endDate, result.getEndDate());
        assertEquals(1, result.getUnavailableDates().size());
        assertFalse(result.isFullyAvailable());
        assertFalse(result.isFullyBooked());
    }
}
