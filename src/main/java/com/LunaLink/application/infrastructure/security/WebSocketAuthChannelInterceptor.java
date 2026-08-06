package com.LunaLink.application.infrastructure.security;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
public class WebSocketAuthChannelInterceptor {

    public org.springframework.messaging.support.ChannelInterceptor channelInterceptor() {
        return new org.springframework.messaging.support.ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && requiresAuthentication(accessor.getCommand())
                        && (accessor.getUser() == null || accessor.getUser().getName() == null)) {
                    throw new MessageDeliveryException("Não autenticado");
                }
                return message;
            }
        };
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
