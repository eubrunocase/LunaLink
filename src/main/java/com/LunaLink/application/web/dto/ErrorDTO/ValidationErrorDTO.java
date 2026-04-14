package com.LunaLink.application.web.dto.ErrorDTO;

import java.time.LocalDateTime;
import java.util.Map;

public record ValidationErrorDTO(
        LocalDateTime timestamp,
        Integer status,
        String error,
        String message,
        Map<String, String> validationErrors,
        String path
) {
}
