package com.LunaLink.application.application.businnesRules;

import com.LunaLink.application.core.Administrator;
import com.LunaLink.application.infrastructure.repository.AdministratorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdministratorService extends BaseService<Administrator> {

    @Autowired
    private AdministratorRepository administratorRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    public AdministratorService(JpaRepository<Administrator, Long> repository) {
        super(repository);
    }

    public Administrator findByLogin(String login) {
        Administrator administrator = administratorRepository.findByLogin(login);
        return administrator;
    }

    public Administrator createAdministrator(Administrator administrator) {
        administrator.setPassword(bCryptPasswordEncoder.encode(administrator.getPassword()));
        return administratorRepository.save(administrator);
    }

    public void deleteAdministrator(Long id) {
        Administrator administrator = this.findById(id);
        administratorRepository.delete(administrator);
    }

    public Administrator updateAdministrator(Long id, Administrator administrator) {
        Administrator adm = this.findById(id);
        adm.setLogin(administrator.getLogin());
        adm.setPassword(bCryptPasswordEncoder.encode(administrator.getPassword()));
        return administratorRepository.save(adm);
    }
}
