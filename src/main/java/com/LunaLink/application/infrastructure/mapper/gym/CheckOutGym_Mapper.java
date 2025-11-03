package com.LunaLink.application.core.infrastructure.persistence.gym.checkOut;

import com.LunaLink.application.domain.model.gym.CheckOut_Gym;
import com.LunaLink.application.web.dto.checkOut_gym_DTO.CheckOut_Gym_ResponseDTO;

import java.util.List;

public interface CheckOutGym_Mapper {

    CheckOut_Gym_ResponseDTO toDTO(CheckOut_Gym checkOut_gym);
    CheckOut_Gym toEntity(CheckOut_Gym_ResponseDTO checkOut_gym_ResponseDTO);
    List<CheckOut_Gym_ResponseDTO> toDTOList(List<CheckOut_Gym> checkOut_gyms);
}
