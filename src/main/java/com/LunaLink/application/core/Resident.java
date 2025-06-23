package com.LunaLink.application.core;

import com.LunaLink.application.core.enums.UserRoles;
import jakarta.persistence.Entity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "resident")
@EqualsAndHashCode(of = "id")
public class Resident extends Users{

    public Resident(String login, String password, UserRoles role) {
        super(login, password, role);
    }

    public Resident() {
        super("", "", UserRoles.RESIDENT);
    }

    @Override
    public String toString() {
        return "Resident{}";
    }

    @Override
    public String getUsername() {
        return this.getLogin();
    }

    @Override
    public boolean isAccountNonExpired() {
        return super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled();
    }
}
