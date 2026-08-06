package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.facades.auth.LoginFacade;
import com.LunaLink.application.application.service.auth.AuthenticationService;
import com.LunaLink.application.infrastructure.security.BearerTokenUtils;
import com.LunaLink.application.web.dto.SecurityDTO.AuthenticationDTO;
import com.LunaLink.application.web.dto.SecurityDTO.LoginResponseDTO;
import com.LunaLink.application.web.dto.SecurityDTO.LogoutRequestDTO;
import com.LunaLink.application.web.dto.SecurityDTO.RefreshRequestDTO;
import com.LunaLink.application.web.dto.SecurityDTO.RefreshResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lunaLink/auth")
public class AuthenticationController {

    private final LoginFacade loginFacade;
    private final AuthenticationService authenticationService;

    public AuthenticationController(LoginFacade loginFacade,
                                    AuthenticationService authenticationService) {
        this.loginFacade = loginFacade;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid AuthenticationDTO data,
                                                  HttpServletRequest request) {
        return loginFacade.login(data, getClientIp(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponseDTO> refresh(@RequestBody @Valid RefreshRequestDTO data) {
        return ResponseEntity.ok(authenticationService.refresh(data.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody(required = false) @Valid LogoutRequestDTO data,
                                       HttpServletRequest request) {
        String accessToken = BearerTokenUtils.extract(request);
        String refreshToken = data != null ? data.refreshToken() : null;
        authenticationService.logout(refreshToken, accessToken);
        return ResponseEntity.ok().build();
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
