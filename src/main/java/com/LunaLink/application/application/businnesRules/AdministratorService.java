package com.LunaLink.application.application.businnesRules;

import com.LunaLink.application.core.Administrator;
import com.LunaLink.application.infrastructure.repository.administrator.AdministratorMapper;
import com.LunaLink.application.infrastructure.repository.administrator.AdministratorRepository;
import com.LunaLink.application.web.dto.AdministratorDTO.AdmnistratorResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdministratorService extends BaseService<Administrator> {

    private final AdministratorRepository administratorRepository;
    private final AdministratorMapper administratorMapper;

    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    public AdministratorService(JpaRepository<Administrator, Long> repository,
                                AdministratorMapper administratorMapper,
                                AdministratorRepository administratorRepository) {
        super(repository);
        this.administratorMapper = administratorMapper;
        this.administratorRepository = administratorRepository;
    }

    public Administrator findAdmByLogin(String login) {
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


    public List<AdmnistratorResponseDTO> findAllAdm () {
        List<Administrator> administrators = administratorRepository.findAll();
        return administratorMapper.toDTOList(administrators);
    }
}
