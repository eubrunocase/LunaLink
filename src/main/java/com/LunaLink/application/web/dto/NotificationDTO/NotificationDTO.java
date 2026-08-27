package com.LunaLink.application.web.dto.NotificationDTO;

import java.time.LocalDateTime;

public record NotificationDTO(String title,
                              String message,
                              String type, // Ex: "RESERVATION_CREATED", "RESERVATION_AWAITING_INSPECTION"
                              LocalDateTime timestamp) {
}
