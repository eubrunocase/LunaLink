package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.service.reservation.AvailabilityService;
import com.LunaLink.application.infrastructure.mapper.reservation.AvailabilityMapper;
import com.LunaLink.application.web.dto.ReservationsDTO.availabilityReservations.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/lunaLink/availabilitySpaces/{spaceId}/availability")
public class AvailabilityController {

    private static final Logger logger = LoggerFactory.getLogger(AvailabilityController.class);

    private final AvailabilityService availabilityService;
    private final AvailabilityMapper availabilityMapper;

    public AvailabilityController(AvailabilityService availabilityService, AvailabilityMapper availabilityMapper) {
        this.availabilityService = availabilityService;
        this.availabilityMapper = availabilityMapper;
    }

    /**
     * Verifica disponibilidade de uma data específica
     *
     * @param spaceId ID do espaço
     * @param date Data a verificar (formato: yyyy-MM-dd)
     * @return DTO com status de disponibilidade
     *
     * Exemplo:
     * GET /api/v1/spaces/1/availability/status?date=2026-01-15
     *
     * Resposta 200:
     * {
     *   "spaceId": 1,
     *   "date": "2026-01-15",
     *   "available": true
     * }
     *
     * Status HTTP:
     * - 200 OK: Disponibilidade verificada com sucesso
     * - 400 BAD REQUEST: Parâmetros inválidos
     * - 404 NOT FOUND: Espaço não encontrado
     */
    @GetMapping("/status")
    public ResponseEntity<AvailabilityResponseDTO> checkAvailabilityStatus(
            @PathVariable @NotNull Long spaceId,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        logger.info("Verificando disponibilidade - spaceId: {}, date: {}", spaceId, date);

        try {
            AvailabilityResponseDTO serviceResponse = availabilityService
                    .getAvailabilityStatus(spaceId, date);

            AvailabilityResponseDTO response = availabilityMapper
                    .mapToResponseDTO(serviceResponse);

            logger.info("Disponibilidade verificada - spaceId: {}, date: {}, available: {}",
                    spaceId, date, response.available());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao verificar disponibilidade - Parâmetros inválidos: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro ao verificar disponibilidade - spaceId: {}", spaceId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Retorna datas disponíveis e indisponíveis de um mês completo
     *
     * @param spaceId ID do espaço
     * @param year Ano (ex: 2026)
     * @param month Mês (1-12)
     * @return DTO com datas disponíveis/indisponíveis e estatísticas
     *
     * Exemplo:
     * GET /api/v1/spaces/1/availability/month/2026/1
     *
     * Resposta 200:
     * {
     *   "spaceId": 1,
     *   "year": 2026,
     *   "month": 1,
     *   "unavailableDates": ["2026-01-05", "2026-01-10", "2026-01-15"],
     *   "availableDates": ["2026-01-01", "2026-01-02", ...],
     *   "totalDays": 31,
     *   "unavailableCount": 3,
     *   "availableCount": 28,
     *   "occupancyPercentage": 9.68
     * }
     *
     * Status HTTP:
     * - 200 OK: Dados recuperados com sucesso
     * - 400 BAD REQUEST: Mês ou ano inválidos
     */
    @GetMapping("/month/{year}/{month}")
    public ResponseEntity<AvailabilityMonthDTO> getMonthAvailability(
            @PathVariable @NotNull Long spaceId,
            @PathVariable @NotNull @Min(2020) Integer year,
            @PathVariable @NotNull @Min(1) @Max(12) Integer month) {

        logger.info("Buscando disponibilidade do mês - spaceId: {}, year: {}, month: {}",
                spaceId, year, month);

        try {
            // Obter datas indisponíveis
            var unavailableDates = availabilityService.getUnavailableDates(spaceId, year, month);

            // Obter datas disponíveis
            var availableDates = availabilityService.getAvailableDates(spaceId, year, month);

            // Obter estatísticas
            AvailabilityService.OccupancyStatsDTO occupancyStats = availabilityService
                    .getOccupancyStats(spaceId, year, month);

            // Mapear para DTO de resposta
            AvailabilityMonthDTO response = availabilityMapper
                    .mapToMonthAvailabilityDTO(spaceId, year, month, unavailableDates,
                            availableDates, occupancyStats);

            logger.info("Disponibilidade do mês recuperada - spaceId: {}, month: {}/{}, " +
                            "disponíveis: {}, ocupação: {:.2f}%",
                    spaceId, month, year, availableDates.size(),
                    occupancyStats.getOccupancyPercentage());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao buscar disponibilidade - Parâmetros inválidos: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro ao buscar disponibilidade - spaceId: {}", spaceId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Retorna estatísticas de ocupação de um espaço em um mês
     *
     * @param spaceId ID do espaço
     * @param year Ano
     * @param month Mês (1-12)
     * @return DTO com estatísticas de ocupação
     *
     * Exemplo:
     * GET /api/v1/spaces/1/availability/stats/2026/1
     *
     * Resposta 200:
     * {
     *   "spaceId": 1,
     *   "year": 2026,
     *   "month": 1,
     *   "totalDays": 31,
     *   "unavailableDays": 10,
     *   "availableDays": 21,
     *   "occupancyPercentage": 32.26
     * }
     *
     * Status HTTP:
     * - 200 OK: Estatísticas calculadas
     * - 400 BAD REQUEST: Parâmetros inválidos
     */
    @GetMapping("/stats/{year}/{month}")
    public ResponseEntity<OccupancyStatsResponseDTO> getOccupancyStats(
            @PathVariable @NotNull Long spaceId,
            @PathVariable @NotNull @Min(2020) Integer year,
            @PathVariable @NotNull @Min(1) @Max(12) Integer month) {

        logger.info("Calculando estatísticas de ocupação - spaceId: {}, year: {}, month: {}",
                spaceId, year, month);

        try {
            AvailabilityService.OccupancyStatsDTO serviceResponse = availabilityService
                    .getOccupancyStats(spaceId, year, month);

            OccupancyStatsResponseDTO response = availabilityMapper
                    .mapToOccupancyStatsDTO(serviceResponse, spaceId, year, month);

            logger.info("Estatísticas calculadas - spaceId: {}, ocupação: {:.2f}%",
                    spaceId, response.occupancyPercentage());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao calcular estatísticas - Parâmetros inválidos: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro ao calcular estatísticas - spaceId: {}", spaceId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Verifica disponibilidade em um período customizado
     *
     * @param spaceId ID do espaço
     * @param requestDTO DTO com startDate e endDate
     * @return DTO com datas disponíveis/indisponíveis no período
     *
     * Exemplo:
     * POST /api/v1/spaces/1/availability/period
     * {
     *   "startDate": "2026-01-01",
     *   "endDate": "2026-01-31"
     * }
     *
     * Resposta 200:
     * {
     *   "spaceId": 1,
     *   "startDate": "2026-01-01",
     *   "endDate": "2026-01-31",
     *   "totalDaysInPeriod": 31,
     *   "unavailableDates": ["2026-01-05", "2026-01-10"],
     *   "availableDates": ["2026-01-01", "2026-01-02", ...],
     *   "fullyAvailable": false,
     *   "fullyBooked": false
     * }
     *
     * Status HTTP:
     * - 200 OK: Disponibilidade verificada
     * - 400 BAD REQUEST: Período inválido ou datas mal formatadas
     */
    @PostMapping("/period")
    public ResponseEntity<AvailabilityPeriodResponseDTO> checkPeriodAvailability(
            @PathVariable @NotNull Long spaceId,
            @RequestBody @Valid AvailabilityPeriodRequestDTO requestDTO) {

        logger.info("Verificando disponibilidade no período - spaceId: {}, start: {}, end: {}",
                spaceId, requestDTO.startDate(), requestDTO.endDate());

        try {
            AvailabilityService.PeriodAvailabilityDTO serviceResponse = availabilityService
                    .checkPeriodAvailability(spaceId, requestDTO.startDate(), requestDTO.endDate());

            AvailabilityPeriodResponseDTO response = availabilityMapper
                    .mapToPeriodAvailabilityDTO(serviceResponse);

            String status = serviceResponse.isFullyAvailable() ? "TOTALMENTE_DISPONÍVEL" :
                    serviceResponse.isFullyBooked() ? "TOTALMENTE_RESERVADO" :
                            "PARCIALMENTE_DISPONÍVEL";

            logger.info("Período verificado - spaceId: {}, status: {}, dias_disponíveis: {}",
                    spaceId, status, response.availableDates().size());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao verificar período - Parâmetros inválidos: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro ao verificar período - spaceId: {}", spaceId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Manipulador de exceções genérico
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(IllegalArgumentException e) {
        logger.error("Erro de validação: {}", e.getMessage());
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                "VALIDATION_ERROR",
                e.getMessage(),
                System.currentTimeMillis()
        );
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * DTO simples para respostas de erro
     */
    public record ErrorResponseDTO(
            String error,
            String message,
            long timestamp
    ) {}







}
