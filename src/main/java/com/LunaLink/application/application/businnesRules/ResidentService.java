package com.LunaLink.application.application.businnesRules;

import com.LunaLink.application.core.Resident;
import com.LunaLink.application.infrastructure.repository.ResidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ResidentService extends BaseService<Resident> {

    @Autowired
    private ResidentRepository ResidentRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public ResidentService(JpaRepository<Resident, Long> ResidentRepository) {
        super(ResidentRepository);
    }

    public Resident findResidentByLogin(String login) {
        return ResidentRepository.findByLogin(login);
    }

    public Resident createResident(Resident resident) {
        resident.setPassword(encoder.encode(resident.getPassword()));
        return ResidentRepository.save(resident);
    }

    public void deleteResident(Long id) {
        Resident resident = this.findById(id);
        ResidentRepository.delete(resident);
    }

    public Resident updateResident(Long id, Resident resident) {
        Resident residentForUpdate = this.findById(id);
        residentForUpdate.setLogin(resident.getLogin());
        residentForUpdate.setPassword(encoder.encode(resident.getPassword()));
        return ResidentRepository.save(residentForUpdate);
    }


}
