package com.LunaLink.application.core;

import com.LunaLink.application.core.enums.UserRoles;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity(name = "resident")
@EqualsAndHashCode(of = "id")
public class Resident extends Users{

    /*
     *
     *    - - - - - - - -  last att 28/06/2025 00:58 am - - - - - - - - - -
     *
     */
    @OneToMany(mappedBy = "resident", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations = new ArrayList<>();


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
