package com.LunaLink.application.application.service.reservation;

import com.LunaLink.application.application.ports.output.ReservationRepositoryPort;
import com.LunaLink.application.web.dto.ReservationsDTO.availabilityReservations.AvailabilityResponseDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@Service
public class AvailabilityService {

    private final ReservationRepositoryPort reservationRepository;
    private static final Logger logger = Logger.getLogger(AvailabilityService.class.getName());

    public AvailabilityService(ReservationRepositoryPort repository) {
        this.reservationRepository = repository;
    }

    @Transactional
    public List<LocalDate> getUnavailableDates(Long spaceId, Integer year, Integer month) {

        System.out.println("Buscando datas indisponíveis - spaceId: {}, year: {}, month: {}"
        + spaceId + year + month);

        validateInputs(spaceId, year, month);

        long startTime = System.currentTimeMillis();

        List<LocalDate> unavailableDates = reservationRepository
                .findUnavailableDatesBySpaceAndMonth(spaceId, year, month);

        long duration = System.currentTimeMillis() - startTime;

        logQueryPerformance("getUnavailableDates", duration, unavailableDates.size());

        return unavailableDates;
    }

    @Transactional
    public List<LocalDate> getAvailableDates(Long spaceId, Integer year, Integer month) {

        System.out.println("Calculando datas disponíveis - spaceId: {}, year: {}, month: {}"
        + spaceId + year + month);

        validateInputs(spaceId, year, month);

        // Obter datas indisponíveis
        List<LocalDate> unavailableDates = getUnavailableDates(spaceId, year, month);

        // Calcular período do mês
        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();

        // Gerar lista de datas disponíveis
        List<LocalDate> availableDates = new ArrayList<>();
        Set<LocalDate> unavailableSet = new HashSet<>(unavailableDates);

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = LocalDate.of(year, month, day);
            if (!unavailableSet.contains(date)) {
                availableDates.add(date);
            }
        }

        logger.info("Datas disponíveis calculadas - spaceId: {}, month: {}/{}, disponíveis: {}/{}"
        );

        return availableDates;
    }

    /**
     * Verifica se uma data específica está disponível para um espaço
     *
     * @param spaceId ID do espaço
     * @param date Data a verificar
     * @return true se disponível, false se indisponível
     *
     * Performance: ~2-5ms (usa índice)
     */
    @Transactional
    public boolean isDateAvailable(Long spaceId, LocalDate date) {
        System.out.println("DEBUG - Verificando disponibilidade - spaceId: {}, date: {}" + spaceId + date);

        if (spaceId == null || date == null) {
            throw new IllegalArgumentException("SpaceId e date não podem ser nulos");
        }

        if (date.isBefore(LocalDate.now())) {
            System.out.println("Tentativa de verificar data no passado - date: " + date + "");
            return false;
        }

        long startTime = System.nanoTime();

        boolean exists = reservationRepository.existsBySpaceAndDate(spaceId, date);

        long durationMs = (System.nanoTime() - startTime) / 1_000_000;

        System.out.println("DEBUG - Verificação de disponibilidade - resultado: {}, tempo: {}ms - " + date + "" + durationMs);

        return !exists;
    }

    /**
     * Obtém estatísticas de ocupação para um espaço em um período
     *
     * @param spaceId ID do espaço
     * @param year Ano
     * @param month Mês
     * @return DTO com estatísticas de ocupação
     *
     * Exemplo:
     *   {
     *     "totalDays": 31,
     *     "unavailableDays": 10,
     *     "availableDays": 21,
     *     "occupancyPercentage": 32.26
     *   }
     */
    @Transactional
    public OccupancyStatsDTO getOccupancyStats(Long spaceId, Integer year, Integer month) {

        System.out.println("DEBUG - Calculando estatísticas de ocupação - " +
                "spaceId: {}, year: {}, month: {}" + spaceId + year + month);

        validateInputs(spaceId, year, month);

        List<LocalDate> unavailableDates = getUnavailableDates(spaceId, year, month);
        List<LocalDate> availableDates = getAvailableDates(spaceId, year, month);

        int totalDays = unavailableDates.size() + availableDates.size();
        int unavailableDaysCount = unavailableDates.size();
        int availableDaysCount = availableDates.size();

        double occupancyPercentage = totalDays > 0
                ? (unavailableDaysCount * 100.0) / totalDays
                : 0.0;

        OccupancyStatsDTO stats = new OccupancyStatsDTO(
                totalDays,
                unavailableDaysCount,
                availableDaysCount,
                occupancyPercentage
        );

        logger.info("Estatísticas calculadas - spaceId: {}, ocupação: {:.2f}%"
        );

        return stats;
    }

    /**
     * Verifica disponibilidade em um período customizado
     *
     * @param spaceId ID do espaço
     * @param startDate Data inicial (inclusiva)
     * @param endDate Data final (inclusiva)
     * @return DTO com informações de disponibilidade no período
     *
     * Exemplo:
     *   {
     *     "spaceId": 1,
     *     "startDate": "2026-01-01",
     *     "endDate": "2026-01-31",
     *     "totalDaysInPeriod": 31,
     *     "unavailableDates": [2026-01-05, 2026-01-10],
     *     "availableDates": [2026-01-01, 2026-01-02, ...],
     *     "isFullyAvailable": false,
     *     "isFullyBooked": false
     *   }
     */
    @Transactional
    public PeriodAvailabilityDTO checkPeriodAvailability(Long spaceId, LocalDate startDate, LocalDate endDate) {


        System.out.println("DEBUG - Verificando disponibilidade no período - spaceId: {}, start: {}, end: {}"
                + spaceId + startDate + endDate);

        validatePeriodInputs(spaceId, startDate, endDate);

        long startTime = System.currentTimeMillis();

        // Buscar datas indisponíveis no período
        List<LocalDate> unavailableDates = reservationRepository
                .findUnavailableDatesBySpaceAndPeriod(spaceId, startDate, endDate);

        // Calcular datas disponíveis
        List<LocalDate> availableDates = calculateAvailableDatesInPeriod(startDate, endDate, unavailableDates);

        long duration = System.currentTimeMillis() - startTime;

        // Criar DTO com resultado
        PeriodAvailabilityDTO dto = new PeriodAvailabilityDTO(
                spaceId,
                startDate,
                endDate,
                (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1,
                unavailableDates,
                availableDates,
                availableDates.isEmpty(),  // isFullyBooked
                unavailableDates.isEmpty() // isFullyAvailable
        );

        logQueryPerformance("checkPeriodAvailability", duration, unavailableDates.size());

        return dto;
    }

    /**
     * Retorna um resumo simples de disponibilidade
     *
     * @param spaceId ID do espaço
     * @param date Data a verificar
     * @return DTO com status simples de disponibilidade
     */
    @Transactional
    public AvailabilityResponseDTO getAvailabilityStatus(Long spaceId, LocalDate date) {
        System.out.println("Obtendo status de disponibilidade - spaceId: {}, date: {}" + spaceId + date);

        if (spaceId == null || date == null) {
            throw new IllegalArgumentException("SpaceId e date não podem ser nulos");
        }

        boolean available = isDateAvailable(spaceId, date);

        return new AvailabilityResponseDTO(spaceId, date, available);
    }

    // ======================== Helper Methods ========================

    /**
     * Valida inputs para queries de mês
     */
    private void validateInputs(Long spaceId, Integer year, Integer month) {
        if (spaceId == null || spaceId <= 0) {
            throw new IllegalArgumentException("SpaceId deve ser um número positivo");
        }

        if (year == null || year < 2020) {
            throw new IllegalArgumentException("Ano inválido. Deve ser >= 2020");
        }

        if (month == null || month < 1 || month > 12) {
            throw new IllegalArgumentException("Mês inválido. Deve estar entre 1 e 12");
        }
    }

    /**
     * Valida inputs para queries de período
     */
    private void validatePeriodInputs(Long spaceId, LocalDate startDate, LocalDate endDate) {
        if (spaceId == null || spaceId <= 0) {
            throw new IllegalArgumentException("SpaceId deve ser um número positivo");
        }

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Data inicial e final são obrigatórias");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Data inicial não pode ser maior que data final");
        }

        if (startDate.isBefore(LocalDate.now())) {
            System.out.println("WARN - Período inicia no passado - startDate: {}" + startDate);
        }
    }

    /**
     * Calcula datas disponíveis em um período
     */
    private List<LocalDate> calculateAvailableDatesInPeriod(LocalDate startDate, LocalDate endDate,
                                                            List<LocalDate> unavailableDates) {
        List<LocalDate> availableDates = new ArrayList<>();
        Set<LocalDate> unavailableSet = new HashSet<>(unavailableDates);

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (!unavailableSet.contains(current)) {
                availableDates.add(current);
            }
            current = current.plusDays(1);
        }

        return availableDates;
    }

    /**
     * Registra performance da query em log
     */
    private void logQueryPerformance(String methodName, long durationMs, int resultSize) {
        if (durationMs > 100) {
            System.out.println("WARN - Query lenta - " + methodName + ": " +
                    "" + durationMs + "ms, resultados: " + resultSize + "");
        } else {
            System.out.println("DEBUG - Query performance - " + methodName +
                    ": " + durationMs + "ms, resultados: " + resultSize + "");
        }
    }

    // ======================== DTOs ========================

    /**
     * DTO para estatísticas de ocupação
     */
    public static class OccupancyStatsDTO {
        private final int totalDays;
        private final int unavailableDays;
        private final int availableDays;
        private final double occupancyPercentage;

        public OccupancyStatsDTO(int totalDays, int unavailableDays, int availableDays, double occupancyPercentage) {
            this.totalDays = totalDays;
            this.unavailableDays = unavailableDays;
            this.availableDays = availableDays;
            this.occupancyPercentage = occupancyPercentage;
        }

        public int getTotalDays() { return totalDays; }
        public int getUnavailableDays() { return unavailableDays; }
        public int getAvailableDays() { return availableDays; }
        public double getOccupancyPercentage() { return occupancyPercentage; }
    }

    /**
     * DTO para disponibilidade em período customizado
     */
    public static class PeriodAvailabilityDTO {
        private final Long spaceId;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final int totalDaysInPeriod;
        private final List<LocalDate> unavailableDates;
        private final List<LocalDate> availableDates;
        private final boolean fullyBooked;
        private final boolean fullyAvailable;

        public PeriodAvailabilityDTO(Long spaceId, LocalDate startDate, LocalDate endDate,
                                     int totalDaysInPeriod, List<LocalDate> unavailableDates,
                                     List<LocalDate> availableDates, boolean fullyBooked,
                                     boolean fullyAvailable) {
            this.spaceId = spaceId;
            this.startDate = startDate;
            this.endDate = endDate;
            this.totalDaysInPeriod = totalDaysInPeriod;
            this.unavailableDates = unavailableDates;
            this.availableDates = availableDates;
            this.fullyBooked = fullyBooked;
            this.fullyAvailable = fullyAvailable;
        }

        public Long getSpaceId() { return spaceId; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public int getTotalDaysInPeriod() { return totalDaysInPeriod; }
        public List<LocalDate> getUnavailableDates() { return unavailableDates; }
        public List<LocalDate> getAvailableDates() { return availableDates; }
        public boolean isFullyBooked() { return fullyBooked; }
        public boolean isFullyAvailable() { return fullyAvailable; }
    }




}
