package com.LunaLink.application.application.service.gym;

import com.LunaLink.application.domain.model.resident.Resident;
import com.LunaLink.application.domain.model.space.Space;
import com.LunaLink.application.domain.model.gym.CheckIn_Gym;
import com.LunaLink.application.infrastructure.mapper.gym.CheckinGymMapper;
import com.LunaLink.application.infrastructure.repository.space.SpaceRepository;
import com.LunaLink.application.application.ports.input.CheckinGymServicePort;
import com.LunaLink.application.application.ports.output.CheckinGymRepositoryPort;
import com.LunaLink.application.application.ports.output.ResidentRepositoryPort;
import com.LunaLink.application.web.dto.checkIn_gym_DTO.CheckIn_Gym_RequestDTO;
import com.LunaLink.application.web.dto.checkIn_gym_DTO.CheckIn_Gym_ResponseDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CheckinGymService implements CheckinGymServicePort {

    private final CheckinGymRepositoryPort checkInGymRepositoryPort;
    private final CheckinGymMapper checkInGymMapper;
    private final SpaceRepository spaceRepository;
    private final ResidentRepositoryPort residentRepositoryPort;

    public CheckinGymService(CheckinGymRepositoryPort checkInGymRepositoryPort,
                             CheckinGymMapper checkInGymMapper,
                             SpaceRepository spaceRepository,
                             ResidentRepositoryPort residentRepositoryPort
                               ) {
        this.checkInGymRepositoryPort = checkInGymRepositoryPort;
        this.spaceRepository = spaceRepository;
        this.residentRepositoryPort = residentRepositoryPort;
        this.checkInGymMapper = checkInGymMapper;
    }

    @Override
    @Transactional
    public CheckIn_Gym_ResponseDTO createCheckIn_Gym(CheckIn_Gym_RequestDTO data) throws Exception {
        try {
            Resident r = residentRepositoryPort.findById(data.residentId())
                    .orElseThrow(() -> new IllegalArgumentException("ERRO NO METODO CreateCheckIn_Gym da classe de service, Resident not found"));
            Space s = spaceRepository.findSpaceById(data.spaceId())
                    .orElseThrow(() -> new IllegalArgumentException("ERRO NO METODO CreateCheckIn_Gym da classe de service, space not found"));

            if (checkInGymRepositoryPort.existsByResidentAndEntrada(r, data.entrada())) {
                throw new IllegalStateException(
                        String.format("Já existe um Check-In do usuário " + r + "no horário " + data.entrada())
                );
            }

            CheckIn_Gym checkInGym = new CheckIn_Gym();
            checkInGym.setEntrada(data.entrada());
            checkInGym.assignTo(r, s);

            checkInGymRepositoryPort.save(checkInGym);

            return checkInGymMapper.toDTO(checkInGym);

        } catch (Exception e) {
            throw new Exception("Erro ao criar Check-In: " + e.getMessage());
        }
    }

    @Override
    public List<CheckIn_Gym_ResponseDTO> findAllCheckIn_Gyms() {
       List<CheckIn_Gym> checkInGyms = checkInGymRepositoryPort.findAll();
       return checkInGymMapper.toDTOList(checkInGyms);
    }

    @Override
    public CheckIn_Gym_ResponseDTO findCheckIn_GymById(UUID id) {
        CheckIn_Gym checkInGym = checkInGymRepositoryPort.findCheckIn_gymById(id);
        return checkInGymMapper.toDTO(checkInGym);
    }

    @Override
    @Transactional
    public void deleteCheckIn_Gym(UUID id) {
        CheckIn_Gym checkInGym = checkInGymRepositoryPort.findCheckIn_gymById(id);
        if (checkInGym == null) {
            throw new IllegalArgumentException("ERRO NO METODO DeleteCheckIn_Gym da classe de service, CheckIn_Gym not found");
        }
        checkInGymRepositoryPort.deleteById(id);
    }

    public CheckIn_Gym findByUUID(UUID id) {
        return checkInGymRepositoryPort.findCheckIn_gymById(id);
    }

}
