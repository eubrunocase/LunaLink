package com.LunaLink.application.application.businnesRules;

import com.LunaLink.application.infrastructure.repository.monthlyReservation.MonthlyReservationRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.logging.Logger;

@Component
public class MonthlyReservationCleanUpService {

    private static final Logger log = Logger.getLogger(MonthlyReservationCleanUpService.class.getName());

    private final MonthlyReservationRepository monthlyReservationRepository;

    public MonthlyReservationCleanUpService(MonthlyReservationRepository monthlyReservationRepository) {
        this.monthlyReservationRepository = monthlyReservationRepository;
    }

    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void cleanupOldReservations() {
        log.info("Iniciando a tarefa de limpeza de reservas antigas...");

        LocalDateTime sixtyDaysAgo = LocalDateTime.now().minusDays(60);
        monthlyReservationRepository.deleteByCreationDateBefore(sixtyDaysAgo);

        log.info("Tarefa de limpeza de reservas antigas finalizada.");
    }

}
