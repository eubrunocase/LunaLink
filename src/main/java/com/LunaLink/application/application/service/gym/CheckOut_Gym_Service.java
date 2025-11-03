package com.LunaLink.application.application.service.gymServices;

import com.LunaLink.application.domain.model.gym.CheckIn_Gym;
import com.LunaLink.application.domain.model.gym.CheckOut_Gym;
import com.LunaLink.application.core.infrastructure.persistence.gym.checkOut.CheckOutGym_Mapper;
import com.LunaLink.application.application.ports.input.CheckOut_Gym_ServicePort;
import com.LunaLink.application.application.ports.output.CheckIn_gym_RepositoryPort;
import com.LunaLink.application.application.ports.output.CheckOutGym_RepositoryPort;
import com.LunaLink.application.web.dto.checkOut_gym_DTO.CheckOut_Gym_RequestDTO;
import com.LunaLink.application.web.dto.checkOut_gym_DTO.CheckOut_Gym_ResponseDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class CheckOut_Gym_Service implements CheckOut_Gym_ServicePort {

    private final CheckOutGym_RepositoryPort repositoryPort;
    private final CheckOutGym_Mapper mapper;
    private final CheckIn_gym_RepositoryPort checkInGymRepositoryPort;

    public CheckOut_Gym_Service(CheckOutGym_RepositoryPort repositoryPort, CheckOutGym_Mapper mapper,
                                CheckIn_gym_RepositoryPort checkInGymRepositoryPort) {
        this.repositoryPort = repositoryPort;
        this.mapper = mapper;
        this.checkInGymRepositoryPort = checkInGymRepositoryPort;
    }

    @Override
    @Transactional
    public CheckOut_Gym_ResponseDTO createCheckOut_Gym(CheckOut_Gym_RequestDTO checkOut_gym_requestDTO) throws Exception {
        try {
            CheckIn_Gym c = checkInGymRepositoryPort.findCheckIn_gymById(checkOut_gym_requestDTO.CheckIn_Gym_Id());

            if (repositoryPort.existsByCheckInGym_Id(checkOut_gym_requestDTO.CheckIn_Gym_Id())) {
                throw new IllegalStateException(
                        String.format("Já existe um CheckOut para o registro de CheckIn %s", c.getId())
                );
            }

            CheckOut_Gym checkOut = new CheckOut_Gym();
            checkOut.setSaida(checkOut_gym_requestDTO.saida());
            checkOut.AssignTo(c);

            repositoryPort.save(checkOut);
            return mapper.toDTO(checkOut);
        } catch (Exception e) {
            throw new Exception("Erro ao criar CheckOut: " + e.getMessage());
        }
    }

    @Override
    public List<CheckOut_Gym_ResponseDTO> findAllCheckOut_Gyms() {
        List<CheckOut_Gym> checkOutGyms = repositoryPort.findAll();
        return mapper.toDTOList(checkOutGyms);
    }

    @Override
    public CheckOut_Gym_ResponseDTO findCheckOut_GymById(UUID id) {
        CheckOut_Gym checkout = repositoryPort.findCheckOut_gymById(id);
        return mapper.toDTO(checkout);
    }

    @Override
    public void deleteCheckOut_Gym(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("ERRO NO METODO DeleteCheckOut_Gym da classe de service, CheckOut_Gym not found");
        }
        repositoryPort.deleteById(id);
    }
}
