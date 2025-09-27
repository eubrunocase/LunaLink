package com.LunaLink.application.core.services.businnesRules.facades;

import com.LunaLink.application.core.ports.input.CheckIn_Gym_ServicePort;
import com.LunaLink.application.web.dto.checkIn_gym_DTO.CheckIn_Gym_RequestDTO;
import com.LunaLink.application.web.dto.checkIn_gym_DTO.CheckIn_Gym_ResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class CheckInGym_Facade {

    private final CheckIn_Gym_ServicePort service;

    public CheckInGym_Facade(CheckIn_Gym_ServicePort service) {
        this.service = service;
    }

    public CheckIn_Gym_ResponseDTO createCheckIn (CheckIn_Gym_RequestDTO data) throws Exception {
        if (data == null) {
            throw new IllegalArgumentException("Data is null");
        }
        return service.createCheckIn_Gym(data);
    }

    public List<CheckIn_Gym_ResponseDTO> findAllCheckIn_Gyms () {
        List<CheckIn_Gym_ResponseDTO> checkIn_Gyms = service.findAllCheckIn_Gyms();
        return checkIn_Gyms;
    }

    public CheckIn_Gym_ResponseDTO findCheckIn_GymById (UUID id) {
        CheckIn_Gym_ResponseDTO data = service.findCheckIn_GymById(id);
        return data;
    }

    public void deleteCheckIn_Gym (UUID id) {
        service.deleteCheckIn_Gym(id);
    }

}
