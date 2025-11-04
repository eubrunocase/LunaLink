package com.LunaLink.application.application.ports.input;

import com.LunaLink.application.web.dto.checkOut_gym_DTO.CheckOut_Gym_RequestDTO;
import com.LunaLink.application.web.dto.checkOut_gym_DTO.CheckOut_Gym_ResponseDTO;

import java.util.List;
import java.util.UUID;


public interface CheckoutGymServicePort {

 CheckOut_Gym_ResponseDTO createCheckOut_Gym(CheckOut_Gym_RequestDTO checkOut_gym_requestDTO) throws Exception;
 List<CheckOut_Gym_ResponseDTO> findAllCheckOut_Gyms();
 CheckOut_Gym_ResponseDTO findCheckOut_GymById(UUID id);
 void deleteCheckOut_Gym(UUID id);

}
