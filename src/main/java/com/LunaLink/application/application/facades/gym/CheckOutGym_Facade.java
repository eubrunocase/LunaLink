package com.LunaLink.application.application.service.facades;

import com.LunaLink.application.application.ports.input.CheckIn_Gym_ServicePort;
import com.LunaLink.application.application.ports.input.CheckOut_Gym_ServicePort;
import com.LunaLink.application.web.dto.checkOut_gym_DTO.CheckOutCreateDTO;
import com.LunaLink.application.web.dto.checkOut_gym_DTO.CheckOut_Gym_RequestDTO;
import com.LunaLink.application.web.dto.checkOut_gym_DTO.CheckOut_Gym_ResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class CheckOutGym_Facade {

    private final CheckOut_Gym_ServicePort service;
    private final CheckIn_Gym_ServicePort checkInGymService;

    public CheckOutGym_Facade(CheckOut_Gym_ServicePort service, CheckIn_Gym_ServicePort checkInGymService) {
        this.service = service;
        this.checkInGymService = checkInGymService;
    }

    public CheckOut_Gym_ResponseDTO create(CheckOutCreateDTO data, UUID id) throws Exception {
        if (data == null) {
            throw new IllegalArgumentException("Data is null");
        }
        UUID checkIn = checkInGymService.findByUUID(id).getId();
        CheckOut_Gym_RequestDTO request = new CheckOut_Gym_RequestDTO(
                checkIn,
                data.saida()
        );
        return service.createCheckOut_Gym(request);
    }

    public List<CheckOut_Gym_ResponseDTO> findAll() {
        List<CheckOut_Gym_ResponseDTO> checkOut_Gyms = service.findAllCheckOut_Gyms();
        return checkOut_Gyms;
    }

    public CheckOut_Gym_ResponseDTO findById(UUID id) {
        CheckOut_Gym_ResponseDTO data = service.findCheckOut_GymById(id);
        return data;
    }

    public void deleteById(UUID id) {
        service.deleteCheckOut_Gym(id);
    }

}
