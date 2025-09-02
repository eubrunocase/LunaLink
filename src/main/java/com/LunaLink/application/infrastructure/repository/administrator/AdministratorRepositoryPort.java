package com.LunaLink.application.infrastructure.repository.administrator;

import com.LunaLink.application.core.Administrator;
import java.util.List;
import java.util.Optional;

/*
   Abstração para o repositório concreto

   Assinaturas de métodos devem ser IDÊNTICAS as da JPA
 */
public interface AdministratorRepositoryPort {

    Administrator findByLogin(String login);
    Administrator save(Administrator administrator);
    void deleteById(Long id);
    List<Administrator> findAll();
    Optional<Administrator> findById(Long id);

}
