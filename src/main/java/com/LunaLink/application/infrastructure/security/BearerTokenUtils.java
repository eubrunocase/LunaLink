package com.LunaLink.application.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;

public final class BearerTokenUtils {

    private BearerTokenUtils() {
    }

    public static String extract(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.length() <= 7
                || !authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }
        return authHeader.substring(7);
    }
}
