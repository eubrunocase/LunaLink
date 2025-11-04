package com.LunaLink.application.infrastructure.repository.administrator;

import com.LunaLink.application.domain.model.administrator.Administrator;
import com.LunaLink.application.application.ports.output.AdministratorRepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdministratorRepository extends JpaRepository<Administrator, Long>, AdministratorRepositoryPort {
    Administrator findByLogin(String login);
}
