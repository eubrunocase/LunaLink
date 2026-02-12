package com.LunaLink.application.web.dto.UserDTO;

import com.LunaLink.application.domain.enums.UserRoles;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record RequestUserDTO(@JsonProperty("id") UUID id,
                             @JsonProperty("login") String login,
                             @JsonProperty("password") String password,
                             @JsonProperty("role") UserRoles role) {
}
