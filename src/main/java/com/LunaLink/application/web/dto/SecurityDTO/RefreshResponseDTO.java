package com.LunaLink.application.web.dto.SecurityDTO;

public record RefreshResponseDTO(
        String accessToken,
        String refreshToken,
        long expiresIn,
        String tokenType) {
}
