//package com.LunaLink.application;
//
//import com.LunaLink.application.domain.enums.SpaceType;
//import com.LunaLink.application.domain.model.resident.Resident;
//import com.LunaLink.application.domain.model.reservation.Reservation;
//import com.LunaLink.application.domain.model.space.Space;
//import com.LunaLink.application.infrastructure.repository.reservation.ReservationRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.Nested;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.time.LocalDate;
//import java.time.YearMonth;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.*;
//
///**
// * Testes de integração para ReservationRepository
// *
// * Testa:
// * - Queries otimizadas com índices
// * - Performance de busca
// * - Validações de dados
// */
//@DataJpaTest
//@ActiveProfiles("test")
//@DisplayName("ReservationRepository Tests")
//class ReservationRepositoryTest {
//
//    @Autowired
//    private ReservationRepository reservationRepository;
//
//    @Autowired
//    private TestEntityManager entityManager;
//
//    private Space space1;
//    private Space space2;
//    private Resident resident1;
//    private Resident resident2;
//
////    @BeforeEach
////    void setUp() {
////        // Criar dados de teste
////        space1 = createSpace(1);
////        space2 = createSpace(1);
////
////        resident1 = createResident("João Silva");
////        resident2 = createResident("Maria Santos");
////
////        entityManager.flush();
////        entityManager.clear();
////    }
//
//    @Nested
//    @DisplayName("findUnavailableDatesBySpaceAndMonth")
//    class FindUnavailableDatesBySpaceAndMonthTests {
//
//        @Test
//        @DisplayName("Deve retornar datas indisponíveis do espaço em janeiro/2026")
//        void shouldReturnUnavailableDatesForJanuary2026() {
//            // Arrange
//            LocalDate date1 = LocalDate.of(2026, 1, 5);
//            LocalDate date2 = LocalDate.of(2026, 1, 15);
//            LocalDate date3 = LocalDate.of(2026, 1, 25);
//
//            createReservation(resident1, space1, date1);
//            createReservation(resident1, space1, date2);
//            createReservation(resident2, space1, date3);
//
//            entityManager.flush();
//            entityManager.clear();
//
//            // Act
//            List<LocalDate> unavailableDates = reservationRepository
//                    .findUnavailableDatesBySpaceAndMonth(space1.getId(), 2026, 1);
//
//            // Assert
//            assertThat(unavailableDates)
//                    .hasSize(3)
//                    .containsExactly(date1, date2, date3)
//                    .isSorted();
//        }
//
//        @Test
//        @DisplayName("Deve retornar apenas DISTINCT dates (sem duplicatas)")
//        void shouldReturnDistinctDates() {
//            // Arrange - Múltiplas reservas no mesmo dia (diferentes residentes)
//            LocalDate sameDate = LocalDate.of(2026, 1, 10);
//
//            createReservation(resident1, space1, sameDate);
//            createReservation(resident2, space1, sameDate);
//
//            entityManager.flush();
//            entityManager.clear();
//
//            // Act
//            List<LocalDate> unavailableDates = reservationRepository
//                    .findUnavailableDatesBySpaceAndMonth(space1.getId(), 2026, 1);
//
//            // Assert
//            assertThat(unavailableDates)
//                    .hasSize(1)
//                    .contains(sameDate);
//        }
//
//        @Test
//        @DisplayName("Deve retornar lista vazia quando não há reservas")
//        void shouldReturnEmptyListWhenNoReservations() {
//            // Act
//            List<LocalDate> unavailableDates = reservationRepository
//                    .findUnavailableDatesBySpaceAndMonth(space1.getId(), 2026, 2);
//
//            // Assert
//            assertThat(unavailableDates).isEmpty();
//        }
//
//        @Test
//        @DisplayName("Deve retornar apenas datas do espaço específico")
//        void shouldReturnOnlyDatesForSpecificSpace() {
//            // Arrange
//            LocalDate date1 = LocalDate.of(2026, 1, 5);
//            LocalDate date2 = LocalDate.of(2026, 1, 15);
//
//            createReservation(resident1, space1, date1);
//            createReservation(resident2, space2, date2); // Outro espaço
//
//            entityManager.flush();
//            entityManager.clear();
//
//            // Act
//            List<LocalDate> unavailableDates = reservationRepository
//                    .findUnavailableDatesBySpaceAndMonth(space1.getId(), 2026, 1);
//
//            // Assert
//            assertThat(unavailableDates)
//                    .hasSize(1)
//                    .contains(date1)
//                    .doesNotContain(date2);
//        }
//
//        @Test
//        @DisplayName("Deve retornar apenas datas do mês especificado")
//        void shouldReturnOnlyDatesFromSpecificMonth() {
//            // Arrange
//            LocalDate dateJan = LocalDate.of(2026, 1, 5);
//            LocalDate dateFeb = LocalDate.of(2026, 2, 5);
//
//            createReservation(resident1, space1, dateJan);
//            createReservation(resident2, space1, dateFeb);
//
//            entityManager.flush();
//            entityManager.clear();
//
//            // Act
//            List<LocalDate> unavailableDatesJan = reservationRepository
//                    .findUnavailableDatesBySpaceAndMonth(space1.getId(), 2026, 1);
//
//            List<LocalDate> unavailableDatesFeb = reservationRepository
//                    .findUnavailableDatesBySpaceAndMonth(space1.getId(), 2026, 2);
//
//            // Assert
//            assertThat(unavailableDatesJan)
//                    .hasSize(1)
//                    .contains(dateJan);
//
//            assertThat(unavailableDatesFeb)
//                    .hasSize(1)
//                    .contains(dateFeb);
//        }
//
//        @Test
//        @DisplayName("Deve retornar datas ordenadas crescente")
//        void shouldReturnDatesOrderedAscending() {
//            // Arrange
//            LocalDate date1 = LocalDate.of(2026, 1, 25);
//            LocalDate date2 = LocalDate.of(2026, 1, 5);
//            LocalDate date3 = LocalDate.of(2026, 1, 15);
//
//            createReservation(resident1, space1, date1);
//            createReservation(resident2, space1, date2);
//            createReservation(resident1, space1, date3);
//
//            entityManager.flush();
//            entityManager.clear();
//
//            // Act
//            List<LocalDate> unavailableDates = reservationRepository
//                    .findUnavailableDatesBySpaceAndMonth(space1.getId(), 2026, 1);
//
//            // Assert
//            assertThat(unavailableDates)
//                    .containsExactly(date2, date3, date1); // Ordem crescente
//        }
//
//        @Test
//        @DisplayName("Performance: Deve buscar datas em < 50ms com muitos dados")
//        void shouldPerformWellWithLargeDataset() {
//            // Arrange - Cria muitas reservas
//            for (int i = 1; i <= 100; i++) {
//                LocalDate date = LocalDate.of(2026, 1, (i % 28) + 1);
//                Resident resident = createResident("Resident" + i);
//                createReservation(resident, space1, date);
//            }
//
//            entityManager.flush();
//            entityManager.clear();
//
//            // Act
//            long startTime = System.currentTimeMillis();
//            List<LocalDate> unavailableDates = reservationRepository
//                    .findUnavailableDatesBySpaceAndMonth(space1.getId(), 2026, 1);
//            long duration = System.currentTimeMillis() - startTime;
//
//            // Assert
//            assertThat(unavailableDates).isNotEmpty();
//            assertThat(duration)
//                    .as("Query deve ser rápida (< 50ms)")
//                    .isLessThan(50);
//        }
//    }
//
//    @Nested
//    @DisplayName("findUnavailableDatesBySpaceAndPeriod")
//    class FindUnavailableDatesBySpaceAndPeriodTests {
//
//        @Test
//        @DisplayName("Deve retornar datas em período customizado")
//        void shouldReturnDatesInCustomPeriod() {
//            // Arrange
//            LocalDate startDate = LocalDate.of(2026, 1, 1);
//            LocalDate endDate = LocalDate.of(2026, 3, 31);
//
//            LocalDate dateInRange = LocalDate.of(2026, 2, 15);
//            LocalDate dateOutOfRange = LocalDate.of(2026, 4, 15);
//
//            createReservation(resident1, space1, dateInRange);
//            createReservation(resident2, space1, dateOutOfRange);
//
//            entityManager.flush();
//            entityManager.clear();
//
//            // Act
//            List<LocalDate> unavailableDates = reservationRepository
//                    .findUnavailableDatesBySpaceAndPeriod(space1.getId(), startDate, endDate);
//
//            // Assert
//            assertThat(unavailableDates)
//                    .hasSize(1)
//                    .contains(dateInRange)
//                    .doesNotContain(dateOutOfRange);
//        }
//
//        @Test
//        @DisplayName("Deve incluir datas limites (start e end)")
//        void shouldIncludeBoundaryDates() {
//            // Arrange
//            LocalDate startDate = LocalDate.of(2026, 1, 1);
//            LocalDate endDate = LocalDate.of(2026, 1, 31);
//
//            createReservation(resident1, space1, startDate);
//            createReservation(resident2, space1, endDate);
//
//            entityManager.flush();
//            entityManager.clear();
//
//            // Act
//            List<LocalDate> unavailableDates = reservationRepository
//                    .findUnavailableDatesBySpaceAndPeriod(space1.getId(), startDate, endDate);
//
//            // Assert
//            assertThat(unavailableDates)
//                    .hasSize(2)
//                    .contains(startDate, endDate);
//        }
//    }
//
//    @Nested
//    @DisplayName("existsBySpaceAndDate")
//    class ExistsBySpaceAndDateTests {
//
//        @Test
//        @DisplayName("Deve retornar true quando existe reserva")
//        void shouldReturnTrueWhenReservationExists() {
//            // Arrange
//            LocalDate date = LocalDate.of(2026, 1, 10);
//            createReservation(resident1, space1, date);
//
//            entityManager.flush();
//            entityManager.clear();
//
//            // Act
//            boolean exists = reservationRepository
//                    .existsBySpaceAndDate(space1.getId(), date);
//
//            // Assert
//            assertThat(exists).isTrue();
//        }
//
//        @Test
//        @DisplayName("Deve retornar false quando não existe reserva")
//        void shouldReturnFalseWhenReservationDoesNotExist() {
//            // Act
//            boolean exists = reservationRepository
//                    .existsBySpaceAndDate(space1.getId(), LocalDate.of(2026, 1, 10));
//
//            // Assert
//            assertThat(exists).isFalse();
//        }
//
//        @Test
//        @DisplayName("Deve diferenciar espaços")
//        void shouldDifferentiateSpaces() {
//            // Arrange
//            LocalDate date = LocalDate.of(2026, 1, 10);
//            createReservation(resident1, space1, date);
//
//            entityManager.flush();
//            entityManager.clear();
//
//            // Act
//            boolean existsInSpace1 = reservationRepository
//                    .existsBySpaceAndDate(space1.getId(), date);
//            boolean existsInSpace2 = reservationRepository
//                    .existsBySpaceAndDate(space2.getId(), date);
//
//            // Assert
//            assertThat(existsInSpace1).isTrue();
//            assertThat(existsInSpace2).isFalse();
//        }
//    }
//
//    @Nested
//    @DisplayName("findByResidentAndDateRange")
//    class FindByResidentAndDateRangeTests {
//
//        @Test
//        @DisplayName("Deve retornar reservas do residente no período")
//        void shouldReturnResidentReservationsInPeriod() {
//            // Arrange
//            LocalDate date1 = LocalDate.of(2026, 1, 5);
//            LocalDate date2 = LocalDate.of(2026, 1, 15);
//            LocalDate dateOutOfRange = LocalDate.of(2026, 3, 10);
//
//            createReservation(resident1, space1, date1);
//            createReservation(resident1, space2, date2);
//            createReservation(resident1, space1, dateOutOfRange);
//
//            entityManager.flush();
//            entityManager.clear();
//
//            // Act
//            List<Reservation> reservations = reservationRepository
//                    .findByResidentAndDateRange(
//                            resident1.getId(),
//                            LocalDate.of(2026, 1, 1),
//                            LocalDate.of(2026, 1, 31)
//                    );
//
//            // Assert
//            assertThat(reservations)
//                    .hasSize(2)
//                    .extracting(r -> r.getDate())
//                    .contains(date1, date2)
//                    .doesNotContain(dateOutOfRange);
//        }
//
//        @Test
//        @DisplayName("Deve retornar reservas ordenadas por data")
//        void shouldReturnReservationsOrderedByDate() {
//            // Arrange
//            LocalDate date1 = LocalDate.of(2026, 1, 25);
//            LocalDate date2 = LocalDate.of(2026, 1, 5);
//            LocalDate date3 = LocalDate.of(2026, 1, 15);
//
//            createReservation(resident1, space1, date1);
//            createReservation(resident1, space2, date2);
//            createReservation(resident1, space1, date3);
//
//            entityManager.flush();
//            entityManager.clear();
//
//            // Act
//            List<Reservation> reservations = reservationRepository
//                    .findByResidentAndDateRange(
//                            resident1.getId(),
//                            LocalDate.of(2026, 1, 1),
//                            LocalDate.of(2026, 1, 31)
//                    );
//
//            // Assert
//            assertThat(reservations)
//                    .extracting(r -> r.getDate())
//                    .containsExactly(date2, date3, date1);
//        }
//    }
//
//    @Nested
//    @DisplayName("findBySpaceAndMonth")
//    class FindBySpaceAndMonthTests {
//
//        @Test
//        @DisplayName("Deve retornar todas as reservas do espaço no mês")
//        void shouldReturnAllReservationsForSpaceInMonth() {
//            // Arrange
//            LocalDate date1 = LocalDate.of(2026, 1, 5);
//            LocalDate date2 = LocalDate.of(2026, 1, 15);
//
//            createReservation(resident1, space1, date1);
//            createReservation(resident2, space1, date2);
//
//            entityManager.flush();
//            entityManager.clear();
//
//            // Act
//            List<Reservation> reservations = reservationRepository
//                    .findBySpaceAndMonth(space1.getId(), 2026, 1);
//
//            // Assert
//            assertThat(reservations)
//                    .hasSize(2)
//                    .extracting(r -> r.getDate())
//                    .contains(date1, date2);
//        }
//
//        @Test
//        @DisplayName("Deve incluir dados completos da reserva (não só data)")
//        void shouldIncludeCompleteReservationData() {
//            // Arrange
//            LocalDate date = LocalDate.of(2026, 1, 10);
//            createReservation(resident1, space1, date);
//
//            entityManager.flush();
//            entityManager.clear();
//
//            // Act
//            List<Reservation> reservations = reservationRepository
//                    .findBySpaceAndMonth(space1.getId(), 2026, 1);
//
//            // Assert
//            assertThat(reservations)
//                    .hasSize(1)
//                    .first()
//                    .satisfies(r -> {
//                        assertThat(r.getDate()).isEqualTo(date);
//                        assertThat(r.getResident()).isEqualTo(resident1);
//                        assertThat(r.getSpace()).isEqualTo(space1);
//                    });
//        }
//    }
//
//    // ======================== Helper Methods ========================
//
//    private Space createSpace(SpaceType type) {
//        Space space = new Space();
//        space.setType(type);
//        return entityManager.persistAndFlush(space);
//    }
//
//    private Resident createResident(String login) {
//        Resident resident = new Resident();
//        resident.setLogin(login);
//        return entityManager.persistAndFlush(resident);
//    }
//
//    private Reservation createReservation(Resident resident, Space space, LocalDate date) {
//        Reservation reservation = new Reservation();
//        reservation.setResident(resident);
//        reservation.setSpace(space);
//        reservation.setDate(date);
//        return entityManager.persistAndFlush(reservation);
//    }
//}