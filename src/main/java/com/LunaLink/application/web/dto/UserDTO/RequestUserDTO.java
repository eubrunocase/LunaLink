package com.LunaLink.application.web.dto.UserDTO;

import com.LunaLink.application.domain.enums.UserRoles;
import com.fasterxml.jackson.annotation.JsonProperty;

public record RequestUserDTO(@JsonProperty("name") String name,
                             @JsonProperty("apartment") String apartment,
                             @JsonProperty("email") String email,
                             @JsonProperty("password") String password,
                             @JsonProperty("role") UserRoles role) {
}
