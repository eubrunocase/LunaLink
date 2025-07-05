package com.LunaLink.application.core;

import com.LunaLink.application.core.enums.UserRoles;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "administrator")
@EqualsAndHashCode(of = "id")
public class Administrator extends Users{

    public Administrator(String login, String password, UserRoles role) {
        super(login, password, role);
    }

    public Administrator() {
        super("", "", UserRoles.ADMINISTRATOR);
    }

    @Override
    public String toString() {
        return "Administrador{}";
    }
    
    @Override
    public String getUsername() {
        return this.getLogin();
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return super.isAccountNonExpired();
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return super.isAccountNonLocked();
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return super.isCredentialsNonExpired();
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return super.isEnabled();
    }

}
