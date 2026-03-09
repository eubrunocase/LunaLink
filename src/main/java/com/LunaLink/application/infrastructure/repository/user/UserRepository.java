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
    
    Users findByEmailAddress(String email);

    @Override
    default Users findByEmail(String email) {
        return findByEmailAddress(email);
    }

    @Override
    Optional<Users> findById(UUID id);

    @Override
    List<Users> findByRole(UserRoles role);

    @Override
    List<Users> findAll();

}
