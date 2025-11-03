package com.LunaLink.application.core.ports.input;

import com.LunaLink.application.core.domain.gym.CheckIn_Gym;
import com.LunaLink.application.web.dto.checkIn_gym_DTO.CheckIn_Gym_RequestDTO;
import com.LunaLink.application.web.dto.checkIn_gym_DTO.CheckIn_Gym_ResponseDTO;

import java.util.List;
import java.util.UUID;


public interface CheckIn_Gym_ServicePort {

    CheckIn_Gym_ResponseDTO createCheckIn_Gym(CheckIn_Gym_RequestDTO checkInGymRequestDTO) throws Exception;
    List<CheckIn_Gym_ResponseDTO> findAllCheckIn_Gyms();
    CheckIn_Gym_ResponseDTO findCheckIn_GymById(UUID id);
    void deleteCheckIn_Gym(UUID id);
    CheckIn_Gym findByUUID(UUID id);



}
