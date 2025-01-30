package com.example.LunaLink.infra.security;

import com.example.LunaLink.model.UserRoles;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserRoleConverter implements AttributeConverter<UserRoles, String> {


    @Override
    public String convertToDatabaseColumn(UserRoles role) {
        return role.name().toLowerCase();
    }

    @Override
    public UserRoles convertToEntityAttribute(String dbData) {
        return UserRoles.valueOf(dbData.toUpperCase());
    }
}
