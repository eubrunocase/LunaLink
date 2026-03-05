package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.service.reservation.AvailabilityService;
import com.LunaLink.application.infrastructure.mapper.reservation.AvailabilityMapper;
import com.LunaLink.application.web.dto.ReservationsDTO.availabilityReservations.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityControllerTest {

    @Mock
    private AvailabilityService availabilityService;

    @Mock
    private AvailabilityMapper availabilityMapper;

    @InjectMocks
    private AvailabilityController controller;

    @Test
    @DisplayName("Deve verificar status de disponibilidade")
    void checkAvailabilityStatus_ShouldReturnStatus() {
        // Arrange
        Long spaceId = 1L;
        LocalDate date = LocalDate.now();
        AvailabilityResponseDTO serviceResponse = new AvailabilityResponseDTO(spaceId, date, true);
        when(availabilityService.getAvailabilityStatus(spaceId, date)).thenReturn(serviceResponse);
        when(availabilityMapper.mapToResponseDTO(serviceResponse)).thenReturn(serviceResponse);

        // Act
        ResponseEntity<AvailabilityResponseDTO> response = controller.checkAvailabilityStatus(spaceId, date);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().available());
    }

    @Test
    @DisplayName("Deve retornar disponibilidade do mês")
    void getMonthAvailability_ShouldReturnMonth() {
        // Arrange
        Long spaceId = 1L;
        Integer year = 2026;
        Integer month = 1;
        when(availabilityService.getUnavailableDates(spaceId, year, month)).thenReturn(Collections.emptyList());
        when(availabilityService.getAvailableDates(spaceId, year, month)).thenReturn(Collections.emptyList());
        when(availabilityService.getOccupancyStats(spaceId, year, month)).thenReturn(new AvailabilityService.OccupancyStatsDTO(31, 0, 31, 0.0));
        
        AvailabilityMonthDTO mappedResponse = new AvailabilityMonthDTO(spaceId, year, month, Collections.emptyList(), Collections.emptyList(), 31, 0, 31, 0.0);
        when(availabilityMapper.mapToMonthAvailabilityDTO(any(), any(), any(), any(), any(), any())).thenReturn(mappedResponse);

        // Act
        ResponseEntity<AvailabilityMonthDTO> response = controller.getMonthAvailability(spaceId, year, month);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(31, response.getBody().totalDays());
    }

    @Test
    @DisplayName("Deve retornar estatísticas de ocupação")
    void getOccupancyStats_ShouldReturnStats() {
        // Arrange
        Long spaceId = 1L;
        Integer year = 2026;
        Integer month = 1;
        AvailabilityService.OccupancyStatsDTO serviceResponse = new AvailabilityService.OccupancyStatsDTO(31, 0, 31, 0.0);
        when(availabilityService.getOccupancyStats(spaceId, year, month)).thenReturn(serviceResponse);
        
        OccupancyStatsResponseDTO mappedResponse = new OccupancyStatsResponseDTO(spaceId, year, month, 31, 0, 31, 0.0);
        when(availabilityMapper.mapToOccupancyStatsDTO(any(), any(), any(), any())).thenReturn(mappedResponse);

        // Act
        ResponseEntity<OccupancyStatsResponseDTO> response = controller.getOccupancyStats(spaceId, year, month);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0.0, response.getBody().occupancyPercentage());
    }

    @Test
    @DisplayName("Deve verificar disponibilidade do período")
    void checkPeriodAvailability_ShouldReturnPeriod() {
        // Arrange
        Long spaceId = 1L;
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(5);
        AvailabilityPeriodRequestDTO request = new AvailabilityPeriodRequestDTO(startDate, endDate);
        
        AvailabilityService.PeriodAvailabilityDTO serviceResponse = new AvailabilityService.PeriodAvailabilityDTO(spaceId, startDate, endDate, 6, Collections.emptyList(), Collections.emptyList(), true, false);
        when(availabilityService.checkPeriodAvailability(spaceId, startDate, endDate)).thenReturn(serviceResponse);
        
        AvailabilityPeriodResponseDTO mappedResponse = new AvailabilityPeriodResponseDTO(spaceId, startDate, endDate, 6, Collections.emptyList(), Collections.emptyList(), true, false);
        when(availabilityMapper.mapToPeriodAvailabilityDTO(any())).thenReturn(mappedResponse);

        // Act
        ResponseEntity<AvailabilityPeriodResponseDTO> response = controller.checkPeriodAvailability(spaceId, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().fullyAvailable());
    }
}
