package com.LunaLink.application.application.businnesRules;

import com.LunaLink.application.core.Resident;
import com.LunaLink.application.infrastructure.repository.resident.ResidentMapper;
import com.LunaLink.application.infrastructure.repository.resident.ResidentRepository;
import com.LunaLink.application.web.dto.residentDTO.ResidentResponseDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResidentService extends BaseService<Resident> {


    private final ResidentRepository residentRepository;

    private final ResidentMapper residentMapper;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public ResidentService(JpaRepository<Resident, Long> ResidentRepository,
                           ResidentMapper residentMapper,
                           ResidentRepository residentRepository) {
        super(ResidentRepository);
        this.residentMapper = residentMapper;
        this.residentRepository = residentRepository;
    }

    public List<ResidentResponseDTO> findAllResidents() {
        List<Resident> residents = residentRepository.findAll();
        return residentMapper.toDTOList(residents);
    }

    public Resident findResidentByLogin(String login) {
        return residentRepository.findByLogin(login);
    }

    public Resident createResident(Resident resident) {
        resident.setPassword(encoder.encode(resident.getPassword()));
        return residentRepository.save(resident);
    }

    public void deleteResident(Long id) {
        Resident resident = this.findById(id);
        residentRepository.delete(resident);
    }

    public Resident updateResident(Long id, Resident resident) {
        Resident residentForUpdate = this.findById(id);
        residentForUpdate.setLogin(resident.getLogin());
        residentForUpdate.setPassword(encoder.encode(resident.getPassword()));
        return residentRepository.save(residentForUpdate);
    }


}
