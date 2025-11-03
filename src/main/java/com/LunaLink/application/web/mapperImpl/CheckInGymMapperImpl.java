package com.LunaLink.application.web.mapper;

import com.LunaLink.application.domain.model.gym.CheckIn_Gym;
import com.LunaLink.application.core.infrastructure.persistence.gym.checkIn.CheckIn_Gym_Mapper;
import com.LunaLink.application.web.dto.checkIn_gym_DTO.CheckIn_Gym_ResponseDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CheckInGymMapperImpl implements CheckIn_Gym_Mapper {

    @Override
    public CheckIn_Gym_ResponseDTO toDTO(CheckIn_Gym checkIn_gym) {
        if (checkIn_gym == null) {
            return null;
        }
        return new CheckIn_Gym_ResponseDTO(
                checkIn_gym.getId(),
                mapResidentToDTO(checkIn_gym),
                checkIn_gym.getEntrada(),
                mapSpaceToDTO(checkIn_gym)
        );

    }

    @Override
    public List<CheckIn_Gym_ResponseDTO> toDTOList(List<CheckIn_Gym> checkIn_gyms) {
        if (checkIn_gyms == null) {
            return Collections.emptyList();
        }

        return checkIn_gyms.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

    }

    @Override
    public CheckIn_Gym toEntity(CheckIn_Gym_ResponseDTO checkIn_gym_ResponseDTO) {
        if (checkIn_gym_ResponseDTO == null) {
            return null;
        }

        CheckIn_Gym checkIn_gym = new CheckIn_Gym();
        return checkIn_gym;
    }


    private CheckIn_Gym_ResponseDTO.ResidentSummaryDTO mapResidentToDTO(CheckIn_Gym checkIn_gym) {
        if (checkIn_gym.getResident() == null) {
            return null;
        }

        return new CheckIn_Gym_ResponseDTO.ResidentSummaryDTO(
                checkIn_gym.getResident().getId(),
                checkIn_gym.getResident().getLogin()
        );
    }

    private CheckIn_Gym_ResponseDTO.SpaceSummaryDTO mapSpaceToDTO (CheckIn_Gym checkIn_gym) {
        if (checkIn_gym.getSpace() == null) {
            return null;
        }

        return new CheckIn_Gym_ResponseDTO.SpaceSummaryDTO(
                checkIn_gym.getSpace().getId(),
                checkIn_gym.getSpace().getType().toString()
        );

    }

}
