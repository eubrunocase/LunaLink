package com.LunaLink.application.application.ports.output;

import com.LunaLink.application.domain.enums.UserRoles;
import com.LunaLink.application.domain.model.users.Users;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {
    Users findByLogin(String login);
    Optional<Users> findById(UUID id);
    Users save(Users user);
    void deleteById(UUID id);
    List<Users> findByRole(UserRoles role);

}
