package com.LunaLink.application.domain.model.users;

import com.LunaLink.application.domain.enums.UserRoles;
import com.LunaLink.application.domain.model.reservation.Reservation;
import com.LunaLink.application.domain.model.valueObject.Email;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Entity
@Table(name = "users")
@DiscriminatorValue("USER")
@EqualsAndHashCode(of = "id")
public class Users implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid")
    @JsonProperty("id")
    private UUID id;

    @JsonProperty("name")
    @Column(nullable = false)
    private String name;

    @JsonProperty("apartment")
    @Column(name = "apartment")
    private String apartment;

    @Embedded
    @AttributeOverride(name = "address", column = @Column(name = "email", nullable = false, unique = true))
    private Email email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer tokenVersion = 0;

    @JsonProperty("role")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRoles role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations = new ArrayList<>();

    public Users() {
    }

    public Users (String name, String apartment, String email, String password, UserRoles role) {
        this.name = name;
        this.apartment = apartment;
        this.email = new Email(email);
        this.password = password;
        this.role = role;
        this.reservations = new ArrayList<>();
    }

    public UserRoles getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    public String getEmail() {
        return email != null ? email.getAddress() : null;
    }

    public void setEmail(String email) {
        this.email = new Email(email);
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return this.email != null ? this.email.getAddress() : null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApartment() {
        return apartment;
    }

    public void setApartment(String apartment) {
        this.apartment = apartment;
    }

    @Override
    public String toString() {
        return "Users{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", apartment='" + apartment + '\'' +
                ", role=" + role +
                ", tokenVersion=" + tokenVersion +
                '}';
    }

    public void setRole(UserRoles role) {
        this.role = role;
    }

    public Integer getTokenVersion() {
        return tokenVersion != null ? tokenVersion : 0;
    }

    public void setTokenVersion(Integer tokenVersion) {
        this.tokenVersion = tokenVersion;
    }

    public void incrementTokenVersion() {
        this.tokenVersion = (this.tokenVersion == null ? 0 : this.tokenVersion) + 1;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }
}
