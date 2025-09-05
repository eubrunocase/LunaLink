package com.LunaLink.application.core.services.businnesRules;

import com.LunaLink.application.core.domain.Administrator;
import com.LunaLink.application.core.infrastructure.persistence.administrator.AdministratorMapper;
import com.LunaLink.application.core.ports.output.AdministratorRepositoryPort;
import com.LunaLink.application.core.ports.input.AdministratorServicePort;
import com.LunaLink.application.web.dto.AdministratorDTO.AdministratorResponseDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AdministratorService extends BaseService<Administrator> implements AdministratorServicePort {

    private final AdministratorRepositoryPort administratorRepository;
    private final AdministratorMapper administratorMapper;
    private final PasswordEncoder encoder;

    public AdministratorService(JpaRepository<Administrator, Long> repository,
                                AdministratorMapper administratorMapper,
                                AdministratorRepositoryPort administratorRepository,
                                PasswordEncoder encoder) {
        super(repository);
        this.administratorMapper = administratorMapper;
        this.administratorRepository = administratorRepository;
        this.encoder = encoder;
    }

    @Override
    public Administrator findAdmByLogin(String login) {
        return administratorRepository.findByLogin(login);
    }

    @Override
    public Administrator createAdministrator(Administrator administrator) {
        administrator.setPassword(encoder.encode(administrator.getPassword()));
        return administratorRepository.save(administrator);
    }

    @Override
    public void deleteAdministrator(Long id) {
        Administrator administrator = this.findById(id);
        administratorRepository.deleteById(id);
    }

    @Override
    public Administrator updateAdministrator(Long id, Administrator administrator) {
        Administrator adm = this.findById(id);
        adm.setLogin(administrator.getLogin());
        adm.setPassword(encoder.encode(administrator.getPassword()));
        return administratorRepository.save(adm);
    }

    @Override
    public List<AdministratorResponseDTO> findAllAdm () {
        List<Administrator> administrators = administratorRepository.findAll();
        return administratorMapper.toDTOList(administrators);
    }
}
