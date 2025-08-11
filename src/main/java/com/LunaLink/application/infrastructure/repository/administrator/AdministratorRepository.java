package com.LunaLink.application.infrastructure.repository.administrator;

import com.LunaLink.application.core.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdministratorRepository extends JpaRepository<Administrator, Long> {
    Administrator findByLogin(String login);
}
