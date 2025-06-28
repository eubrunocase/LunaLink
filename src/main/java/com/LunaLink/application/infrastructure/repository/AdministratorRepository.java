package com.LunaLink.application.infrastructure.repository;

import com.LunaLink.application.core.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdministratorRepository extends BaseRepository<Administrator> {
    Administrator findByLogin(String login);
}
