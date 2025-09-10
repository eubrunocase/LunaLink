package com.LunaLink.application.core.services.businnesRules.listeners;

import com.LunaLink.application.core.domain.MonthlyReservations;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class MonthlyReservationListener {

    private final RestTemplate restTemplate = new RestTemplate();

    @EventListener
    public void notifyMonthlyReservation(MonthlyReservations event) {
        String url = "http://localhost:8080/lunaLink/monthly-reservation/{id}" + event.getId();
        restTemplate.delete(url, event);
        System.out.println("DELETE ENVIADO PARA O ENDPOINT: " + url + "");
    }

}
