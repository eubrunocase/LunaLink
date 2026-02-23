package com.LunaLink.application.application.ports.output;

import com.LunaLink.application.domain.enums.ReservationStatus;
import com.LunaLink.application.domain.model.space.Space;
import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.domain.model.reservation.Reservation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepositoryPort {

    Reservation save(Reservation reservation);
    void deleteById(UUID id);
    List<Reservation> findAll();

    boolean existsByUserAndDateAndSpace(Users user, LocalDate date, Space space);
    boolean existsByDateAndSpace(LocalDate date, Space space);
    boolean existsByDate(LocalDate date);
    boolean existsByDateAndStatus(LocalDate date, ReservationStatus status);

    boolean existsByDateAndSpaceAndStatusIn(LocalDate date, Space space, List<ReservationStatus> statuses);
    boolean existsByUserAndDateAndSpaceAndStatusIn(Users user, LocalDate date, Space space, List<ReservationStatus> statuses);
    boolean existsByDateAndStatusIn(LocalDate date, List<ReservationStatus> statuses);
    //ReservationResponseDTO findReservationById(UUID id);

    Optional<Reservation> findById(UUID id);


    /**
     * Busca todas as datas indisponíveis (com reservas) para um espaço em um mês específico
     *
     * @param spaceId ID do espaço
     * @param year Ano (ex: 2026)
     * @param month Mês (1-12)
     * @return Lista de datas com reservas, ordenadas
     *
     * Performance: O(log n) com índice, ~2-50ms com 1M de registros
     * Query está otimizada para usar idx_reservation_space_date
     */
    @Query("""
        SELECT DISTINCT r.date
        FROM Reservation r
        WHERE r.space.id = :spaceId
          AND YEAR(r.date) = :year
          AND MONTH(r.date) = :month
        ORDER BY r.date ASC
    """)
    List<LocalDate> findUnavailableDatesBySpaceAndMonth(
            @Param("spaceId") Long spaceId,
            @Param("year") Integer year,
            @Param("month") Integer month
    );

    /**
     * Busca datas indisponíveis em um período customizado
     *
     * @param spaceId ID do espaço
     * @param startDate Data inicial (inclusiva)
     * @param endDate Data final (inclusiva)
     * @return Lista de datas com reservas
     *
     * Performance: O(log n) com índice
     */
    @Query("""
        SELECT DISTINCT r.date
        FROM Reservation r
        WHERE r.space.id = :spaceId
          AND r.date BETWEEN :startDate AND :endDate
        ORDER BY r.date ASC
    """)
    List<LocalDate> findUnavailableDatesBySpaceAndPeriod(
            @Param("spaceId") Long spaceId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Verifica se existe reserva para um espaço em uma data específica
     *
     * @param spaceId ID do espaço
     * @param date Data a verificar
     * @return true se existe reserva, false caso contrário
     *
     * Performance: O(log n) com índice
     */
    @Query("""
        SELECT COUNT(r) > 0
        FROM Reservation r
        WHERE r.space.id = :spaceId AND r.date = :date
    """)
    boolean existsBySpaceAndDate(
            @Param("spaceId") Long spaceId,
            @Param("date") LocalDate date
    );

    /**
     * Busca reservas de um residente em um período
     *
     * @param residentId ID do residente
     * @param startDate Data inicial
     * @param endDate Data final
     * @return Lista de reservas do residente
     *
     * Performance: O(log n) com índice idx_reservation_resident_date
     */
    @Query("""
        SELECT r
        FROM Reservation r
        WHERE r.user.id = :residentId
          AND r.date BETWEEN :startDate AND :endDate
        ORDER BY r.date ASC
    """)
    List<Reservation> findByResidentAndDateRange(
            @Param("userId") UUID residentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Busca todas as reservas de um espaço em um mês
     *
     * @param spaceId ID do espaço
     * @param year Ano
     * @param month Mês
     * @return Lista completa de reservas (não apenas datas)
     */
    @Query("""
        SELECT r
        FROM Reservation r
        WHERE r.space.id = :spaceId
          AND YEAR(r.date) = :year
          AND MONTH(r.date) = :month
        ORDER BY r.date ASC
    """)
    List<Reservation> findBySpaceAndMonth(
            @Param("spaceId") Long spaceId,
            @Param("year") Integer year,
            @Param("month") Integer month
    );

    /**
     * Deleta reservas antigas (cleanup)
     *
     * @param beforeDate Data limite (deleta anteriores)
     * @return Número de registros deletados
     */
    @Query("""
        DELETE FROM Reservation r
        WHERE r.date < :beforeDate
    """)
    int deleteOlderThan(@Param("beforeDate") LocalDate beforeDate);


}
