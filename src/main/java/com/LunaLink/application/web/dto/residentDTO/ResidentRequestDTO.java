package com.LunaLink.application.web.dto.residentDTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResidentRequestDTO(@JsonProperty("id") Long id,
                                 @JsonProperty("login") String login,
                                 @JsonProperty("password") String password) {
}
