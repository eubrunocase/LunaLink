package com.LunaLink.application.infrastructure.security;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Component
public class JwtHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        Principal principal = (Principal) attributes.get(JwtHandshakeInterceptor.PRINCIPAL_ATTRIBUTE);
        return principal != null ? principal : super.determineUser(request, wsHandler, attributes);
    }
}
