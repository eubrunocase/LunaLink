package com.LunaLink.application.infrastructure.repository.user;

import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.domain.enums.UserRoles;
import com.LunaLink.application.domain.model.users.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<Users, UUID>, UserRepositoryPort {
    @Override
    Users findByLogin(String login);

    @Override
    Optional<Users> findById(UUID id);

    Optional<Users> findResidentByLogin(String login);  // manter compatibilidade

    @Override
    List<Users> findByRole(UserRoles role);

}
