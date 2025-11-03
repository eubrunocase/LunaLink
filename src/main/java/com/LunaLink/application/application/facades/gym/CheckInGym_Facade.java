package com.LunaLink.application.application.service.facades;

import com.LunaLink.application.domain.model.resident.Resident;
import com.LunaLink.application.application.ports.input.CheckIn_Gym_ServicePort;
import com.LunaLink.application.application.ports.input.ResidentServicePort;
import com.LunaLink.application.web.dto.checkIn_gym_DTO.CheckInCreateDTO;
import com.LunaLink.application.web.dto.checkIn_gym_DTO.CheckIn_Gym_RequestDTO;
import com.LunaLink.application.web.dto.checkIn_gym_DTO.CheckIn_Gym_ResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class CheckInGym_Facade {

    private final CheckIn_Gym_ServicePort service;
    private final ResidentServicePort residentService;

    public CheckInGym_Facade(CheckIn_Gym_ServicePort service, ResidentServicePort residentService) {
        this.service = service;
        this.residentService = residentService;
    }

    public CheckIn_Gym_ResponseDTO createCheckInGym (CheckInCreateDTO data, String login) throws Exception {
        if (data == null) {
            throw new IllegalArgumentException("Data is null");
        }

        Resident resident = residentService.findResidentByLogin(login);

        CheckIn_Gym_RequestDTO request = new CheckIn_Gym_RequestDTO(
                resident.getId(),
                data.entrada(),
                data.spaceId()
        );

        return service.createCheckIn_Gym(request);
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
