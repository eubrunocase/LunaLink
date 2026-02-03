package com.LunaLink.application.infrastructure.mapper.reservation;

import com.LunaLink.application.application.service.reservation.AvailabilityService;
import com.LunaLink.application.web.dto.ReservationsDTO.availabilityReservations.AvailabilityMonthDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.availabilityReservations.AvailabilityPeriodResponseDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.availabilityReservations.AvailabilityResponseDTO;
import com.LunaLink.application.web.dto.ReservationsDTO.availabilityReservations.OccupancyStatsResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class AvailabilityMapper {

    /**
     * Mapeia resposta simples de disponibilidade ?????? REVISAR
     */
    public AvailabilityResponseDTO mapToResponseDTO(
            AvailabilityResponseDTO serviceDto) {
        return new AvailabilityResponseDTO(
                serviceDto.spaceId(),
                serviceDto.date(),
                serviceDto.available()
        );
    }

    /**
     * Mapeia estatísticas de ocupação para DTO de resposta
     */
    public OccupancyStatsResponseDTO mapToOccupancyStatsDTO(
            AvailabilityService.OccupancyStatsDTO serviceDto,
            Long spaceId,
            Integer year,
            Integer month) {
        return new OccupancyStatsResponseDTO(
                spaceId,
                year,
                month,
                serviceDto.getTotalDays(),
                serviceDto.getUnavailableDays(),
                serviceDto.getAvailableDays(),
                serviceDto.getOccupancyPercentage()
        );
    }

    /**
     * Mapeia disponibilidade em período para DTO de resposta
     */
    public AvailabilityPeriodResponseDTO mapToPeriodAvailabilityDTO(
            AvailabilityService.PeriodAvailabilityDTO serviceDto) {
        return new AvailabilityPeriodResponseDTO(
                serviceDto.getSpaceId(),
                serviceDto.getStartDate(),
                serviceDto.getEndDate(),
                serviceDto.getTotalDaysInPeriod(),
                serviceDto.getUnavailableDates(),
                serviceDto.getAvailableDates(),
                serviceDto.isFullyAvailable(),
                serviceDto.isFullyBooked()
        );
    }

    /**
     * Mapeia disponibilidade de mês inteiro para DTO de resposta
     */
    public AvailabilityMonthDTO mapToMonthAvailabilityDTO(
            Long spaceId,
            Integer year,
            Integer month,
            java.util.List<java.time.LocalDate> unavailableDates,
            java.util.List<java.time.LocalDate> availableDates,
            AvailabilityService.OccupancyStatsDTO occupancyStats) {
        return new AvailabilityMonthDTO(
                spaceId,
                year,
                month,
                unavailableDates,
                availableDates,
                occupancyStats.getTotalDays(),
                occupancyStats.getUnavailableDays(),
                occupancyStats.getAvailableDays(),
                occupancyStats.getOccupancyPercentage()
        );
    }
}


