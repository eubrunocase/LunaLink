package com.LunaLink.application.core.infrastructure.persistence.gym.checkIn;

import com.LunaLink.application.domain.model.gym.CheckIn_Gym;
import com.LunaLink.application.web.dto.checkIn_gym_DTO.CheckIn_Gym_ResponseDTO;
import java.util.List;

public interface CheckIn_Gym_Mapper {

    CheckIn_Gym_ResponseDTO toDTO(CheckIn_Gym checkIn_gym);
    List<CheckIn_Gym_ResponseDTO> toDTOList(List<CheckIn_Gym> checkIn_gyms);
    CheckIn_Gym toEntity(CheckIn_Gym_ResponseDTO checkIn_gym_ResponseDTO);
}
