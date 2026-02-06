package com.LunaLink.application.web.mapperImpl;

import com.LunaLink.application.domain.model.gym.CheckOut_Gym;
import com.LunaLink.application.infrastructure.mapper.gym.CheckoutGymMapper;
import com.LunaLink.application.web.dto.checkOut_gym_DTO.CheckOut_Gym_ResponseDTO;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CheckoutGymMapperImpl implements CheckoutGymMapper {

    @Override
    public CheckOut_Gym_ResponseDTO toDTO(CheckOut_Gym checkOut_gym) {
        if (checkOut_gym == null) {
            return null;
        }

        return new CheckOut_Gym_ResponseDTO(
                checkOut_gym.getId(),
                mapCheckIn_GymToDTO(checkOut_gym),
                checkOut_gym.getSaida()
        );
    }

    @Override
    public CheckOut_Gym toEntity(CheckOut_Gym_ResponseDTO checkOut_gym_ResponseDTO) {
        if (checkOut_gym_ResponseDTO == null) {
            return null;
        }

        CheckOut_Gym checkOut_gym = new CheckOut_Gym();
        return checkOut_gym;
    }

    @Override
    public List<CheckOut_Gym_ResponseDTO> toDTOList(List<CheckOut_Gym> checkOut_gyms) {
        if (checkOut_gyms == null) {
            return null;
        }

        return checkOut_gyms.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private CheckOut_Gym_ResponseDTO.CheckIn_Gym_SummaryDTO mapCheckIn_GymToDTO(CheckOut_Gym checkOut_gym) {
        if (checkOut_gym == null) {
            return null;
        }

        return new CheckOut_Gym_ResponseDTO.CheckIn_Gym_SummaryDTO(checkOut_gym.getId());
    }

}
