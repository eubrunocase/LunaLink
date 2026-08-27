package com.LunaLink.application.application.service.liabilityterm;

import com.LunaLink.application.application.ports.input.LiabilityTermServicePort;
import com.LunaLink.application.application.ports.output.LiabilityTermRepositoryPort;
import com.LunaLink.application.application.ports.output.ReservationRepositoryPort;
import com.LunaLink.application.domain.enums.ReservationStatus;
import com.LunaLink.application.domain.events.reservationEvents.ReservationConfirmedEvent;
import com.LunaLink.application.domain.model.reservation.LiabilityTerm;
import com.LunaLink.application.domain.model.reservation.Reservation;
import com.LunaLink.application.infrastructure.eventPublisher.EventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class LiabilityTermService implements LiabilityTermServicePort {

    private static final String DEFAULT_TERM_CONTENT =
            "Eu, residente do condomínio, declaro estar ciente das regras de uso do espaço comum reservado, "
                    + "comprometo-me a zelar pelo bom estado dos equipamentos e a responsabilizar-me por quaisquer "
                    + "danos causados durante o período de utilização.";

    private final LiabilityTermRepositoryPort liabilityTermRepository;
    private final ReservationRepositoryPort reservationRepository;
    private final EventPublisher publisher;

    public LiabilityTermService(LiabilityTermRepositoryPort liabilityTermRepository,
                                ReservationRepositoryPort reservationRepository,
                                EventPublisher publisher) {
        this.liabilityTermRepository = liabilityTermRepository;
        this.reservationRepository = reservationRepository;
        this.publisher = publisher;
    }

    @Transactional
    @Override
    public void signTerm(UUID reservationId, UUID residentId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva não encontrada."));

        if (!reservation.getUser().getId().equals(residentId)) {
            throw new IllegalStateException("Apenas o morador responsável pela reserva pode assinar o termo.");
        }

        if (reservation.getStatus() != ReservationStatus.AWAITING_SIGNATURE) {
            throw new IllegalStateException("Reserva não está aguardando assinatura do termo.");
        }

        LiabilityTerm term = reservation.getLiabilityTerm();
        if (term == null) {
            term = new LiabilityTerm(DEFAULT_TERM_CONTENT, reservation);
        }

        if (term.isSignedByResident()) {
            throw new IllegalStateException("Termo já foi assinado.");
        }

        term.sign();
        liabilityTermRepository.save(term);

        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);

        ReservationConfirmedEvent event = new ReservationConfirmedEvent(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getDate(),
                reservation.getSpace()
        );
        publisher.publishEvent(event);
    }
}
