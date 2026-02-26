package com.LunaLink.application.application.listeners;

import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.domain.enums.UserRoles;
import com.LunaLink.application.domain.events.ReservationApprovedEvent;
import com.LunaLink.application.domain.events.ReservationRequestedEvent;
import com.LunaLink.application.domain.model.users.Users;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ReservationEventListener {

    private final UserRepositoryPort repository;

    public ReservationEventListener(UserRepositoryPort repository) {
        this.repository = repository;
    }

    @Async
    @EventListener
    public void handleReservationRequestedEvent(ReservationRequestedEvent event) {
        List<Users> admins = repository.findByRole(UserRoles.ADMIN_ROLE);

        for (Users admin : admins) {
            System.out.println("========================================");
            System.out.println("NOTIFICAÇÃO PARA O ADMIN: " + admin.getLogin());
            System.out.println("Nova solicitação de reserva pendente de aprovação!");
            System.out.println("Reserva ID: " + event.getReservationId());
            System.out.println("Residente: " + event.getUserId());
            System.out.println("Espaço: " + event.getSpace().getType());
            System.out.println("Data: " + event.getDate());
            System.out.println("========================================");
        }
    }

    @Async
    @EventListener
    public void handleReservationApprovedEvent(ReservationApprovedEvent event) {
        Optional<Users> residentOpt = repository.findById(event.getUserId());

        if (residentOpt.isPresent()) {
            Users resident = residentOpt.get();

            System.out.println("========================================");
            System.out.println("NOTIFICAÇÃO PARA O RESIDENTE: " + resident.getLogin());
            System.out.println("Sua solicitação de reserva foi APROVADA pelo administrador!");
            System.out.println("Reserva ID: " + event.getReservationId());
            System.out.println("Espaço: " + event.getSpace().getType());
            System.out.println("Data: " + event.getDate());
            System.out.println("========================================");

        } else {
            System.err.println("Aviso: Tentativa de notificar morador sobre aprovação, mas usuário ID " + event.getUserId() + " não foi encontrado.");
        }
    }

}
