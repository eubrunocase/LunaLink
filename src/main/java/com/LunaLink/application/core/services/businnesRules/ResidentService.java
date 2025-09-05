package com.LunaLink.application.core.services.businnesRules;

import com.LunaLink.application.core.domain.Resident;
import com.LunaLink.application.core.infrastructure.persistence.resident.ResidentMapper;
import com.LunaLink.application.core.ports.output.ResidentRepositoryPort;
import com.LunaLink.application.core.ports.input.ResidentServicePort;
import com.LunaLink.application.web.dto.residentDTO.ResidentResponseDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResidentService extends BaseService<Resident> implements ResidentServicePort {


    private final ResidentRepositoryPort residentRepository;
    private final ResidentMapper residentMapper;
    private final PasswordEncoder encoder;

    public ResidentService(JpaRepository<Resident, Long> ResidentRepository,
                           ResidentMapper residentMapper,
                           ResidentRepositoryPort residentRepository,
                           PasswordEncoder encoder) {
        super(ResidentRepository);
        this.residentMapper = residentMapper;
        this.residentRepository = residentRepository;
        this.encoder = encoder;
    }

    @Override
    public List<ResidentResponseDTO> findAllResidents() {
        List<Resident> residents = residentRepository.findAll();
        return residentMapper.toDTOList(residents);
    }

    @Override
    public Resident findResidentByLogin(String login) {
        return residentRepository.findByLogin(login);
    }

    @Override
    public Resident createResident(Resident resident) {
        resident.setPassword(encoder.encode(resident.getPassword()));
        return residentRepository.save(resident);
    }

    @Override
    public void deleteResident(Long id) {
        Resident resident = this.findById(id);
        residentRepository.deleteById(id);
    }

    @Override
    public Resident updateResident(Long id, Resident resident) {
        Resident residentForUpdate = this.findById(id);
        residentForUpdate.setLogin(resident.getLogin());
        residentForUpdate.setPassword(encoder.encode(resident.getPassword()));
        return residentRepository.save(residentForUpdate);
    }


}
