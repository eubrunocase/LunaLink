package com.LunaLink.application.web.dto.SecurityDTO;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequestDTO(
        @NotBlank(message = "O refresh token é obrigatório") String refreshToken) {
}
