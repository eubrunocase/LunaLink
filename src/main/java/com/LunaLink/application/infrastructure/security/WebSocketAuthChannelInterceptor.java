package com.LunaLink.application.infrastructure.security;

import com.LunaLink.application.domain.model.users.Users;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class WebSocketAuthChannelInterceptor {

    private static final String NOTIFICATION_TOPIC_PREFIX = "/topic/notifications/";

    public org.springframework.messaging.support.ChannelInterceptor channelInterceptor() {
        return new org.springframework.messaging.support.ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && requiresAuthentication(accessor.getCommand())
                        && (accessor.getUser() == null || accessor.getUser().getName() == null)) {
                    throw new MessageDeliveryException("Não autenticado");
                }
                if (accessor != null && accessor.getCommand() == StompCommand.SUBSCRIBE
                        && !isOwnerOfTopic(accessor)) {
                    throw new MessageDeliveryException("Sem permissão para este tópico");
                }
                return message;
            }
        };
    }

    private boolean isOwnerOfTopic(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith(NOTIFICATION_TOPIC_PREFIX)) {
            return true;
        }
        String targetId = destination.substring(NOTIFICATION_TOPIC_PREFIX.length());
        UUID targetUuid;
        try {
            targetUuid = UUID.fromString(targetId);
        } catch (IllegalArgumentException e) {
            return false;
        }

        Object principal = accessor.getUser() != null ? accessor.getUser() : null;
        if (principal instanceof Authentication authentication) {
            principal = authentication.getPrincipal();
        }
        if (principal instanceof Users user) {
            return targetUuid.equals(user.getId());
        }
        return false;
    }

    private boolean requiresAuthentication(StompCommand command) {
        if (command == null) {
            return false;
        }
        return command == StompCommand.CONNECT
                || command == StompCommand.SUBSCRIBE
                || command == StompCommand.SEND;
    }
}
