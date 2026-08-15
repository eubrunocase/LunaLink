package com.LunaLink.application.infrastructure.security;

import com.LunaLink.application.application.service.notification.WebPushService;
import com.LunaLink.application.domain.model.users.Users;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regressione: o MvcRequestMatcher não casa requisições de upgrade WebSocket,
 * fazendo-as cair em anyRequest().authenticated() (AuthorizationDeniedException).
 * Com PathPatternRequestMatcher o upgrade deve passar pelo AuthorizationFilter.
 */
@SpringBootTest
@AutoConfigureMockMvc
class WebSocketSecurityRegressionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WebPushService webPushService;

    @MockitoBean
    private TokenAuthenticator tokenAuthenticator;

    @Test
    @DisplayName("upgrade websocket com access_token deve passar pelo AuthorizationFilter")
    void websocketUpgradeIsNotDenied() throws Exception {
        when(tokenAuthenticator.authenticate(anyString())).thenReturn(mock(Users.class));

        mockMvc.perform(get("/ws-lunalink/xyz/abc/websocket?access_token=valid.token")
                        .header("Origin", "http://localhost:8100")
                        .header("Upgrade", "websocket")
                        .header("Connection", "Upgrade"))
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertNotEquals(401, result.getResponse().getStatus()));
    }

    @Test
    @DisplayName("handshake do endpoint /ws-lunalink deve passar pelo AuthorizationFilter")
    void handshakeIsNotDenied() throws Exception {
        when(tokenAuthenticator.authenticate(anyString())).thenReturn(mock(Users.class));

        mockMvc.perform(get("/ws-lunalink?access_token=valid.token")
                        .header("Origin", "http://localhost:8100")
                        .header("Upgrade", "websocket")
                        .header("Connection", "Upgrade"))
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertNotEquals(401, result.getResponse().getStatus()));
    }

    @Test
    @DisplayName("/ws-lunalink/info continua acessível")
    void infoIsReachable() throws Exception {
        mockMvc.perform(get("/ws-lunalink/info?access_token=valid.token")
                        .header("Origin", "http://localhost:8100"))
                .andExpect(status().isOk());
    }
}
