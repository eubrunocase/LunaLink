package com.LunaLink.application.application.service.notification;

import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.domain.model.users.PushSubscription;
import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.infrastructure.repository.user.PushSubscriptionRepository;
import com.LunaLink.application.web.dto.NotificationDTO.NotificationDTO;
import com.LunaLink.application.web.dto.NotificationDTO.PushSubscriptionRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.List;

@Service
public class WebPushService {

    @Value("${webpush.vapid.public.key}")
    private String publicKey;

    @Value("${webpush.vapid.private.key}")
    private String privateKey;

    @Value("${webpush.vapid.subject}")
    private String subject;

    private PushService pushService;
    private final PushSubscriptionRepository subscriptionRepository;
    private final UserRepositoryPort userRepository;
    private final ObjectMapper objectMapper;

    public WebPushService(PushSubscriptionRepository subscriptionRepository, UserRepositoryPort userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @PostConstruct
    private void init() throws GeneralSecurityException {
        Security.addProvider(new BouncyCastleProvider());
        pushService = new PushService(publicKey, privateKey, subject);
    }

    @Transactional
    public void subscribe(String userEmail, PushSubscriptionRequestDTO dto) {
        Users user = userRepository.findByEmail(userEmail);
        if (user == null) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }

        // Verifica se já existe para não duplicar
        if (subscriptionRepository.findByEndpoint(dto.endpoint()).isEmpty()) {
            PushSubscription subscription = new PushSubscription(
                    user,
                    dto.endpoint(),
                    dto.keys().p256dh(),
                    dto.keys().auth()
            );
            subscriptionRepository.save(subscription);
        }
    }

    @Transactional
    public void unsubscribe(String endpoint) {
        subscriptionRepository.deleteByEndpoint(endpoint);
    }

    @Async
    public void sendPushNotificationToUser(Users user, NotificationDTO payload) {
        List<PushSubscription> subscriptions = subscriptionRepository.findAllByUser(user);
        
        for (PushSubscription sub : subscriptions) {
            sendPushNotification(sub, payload);
        }
    }

    private void sendPushNotification(PushSubscription sub, NotificationDTO payload) {
        try {
            Subscription.Keys keys = new Subscription.Keys(sub.getP256dh(), sub.getAuth());
            Subscription subscription = new Subscription(sub.getEndpoint(), keys);
            
            String jsonPayload = objectMapper.writeValueAsString(payload);
            
            Notification notification = new Notification(subscription, jsonPayload);
            pushService.send(notification);
            
        } catch (Exception e) {
            System.err.println("Erro ao enviar Web Push para o endpoint: " + sub.getEndpoint() + " - " + e.getMessage());
            // Opcional: Se o erro for 404 (Not Found) ou 410 (Gone), a inscrição expirou no navegador.
            // Poderíamos deletar a inscrição do banco de dados aqui.
            if (e.getMessage().contains("404") || e.getMessage().contains("410")) {
                subscriptionRepository.delete(sub);
            }
        }
    }

    public String getPublicKey() {
        return publicKey;
    }
}
