package com.LunaLink.application.core.infrastructure.persistence.administrator;

import com.LunaLink.application.core.domain.Administrator;
import com.LunaLink.application.core.ports.output.AdministratorRepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdministratorRepository extends JpaRepository<Administrator, Long>, AdministratorRepositoryPort {
    Administrator findByLogin(String login);
}
