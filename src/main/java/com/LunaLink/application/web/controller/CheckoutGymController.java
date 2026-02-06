package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.facades.gym.CheckOutGym_Facade;
import com.LunaLink.application.web.dto.checkOut_gym_DTO.CheckOutCreateDTO;
import com.LunaLink.application.web.dto.checkOut_gym_DTO.CheckOut_Gym_ResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/lunaLink/checkOutGym")
public class CheckoutGymController {

    private final CheckOutGym_Facade facade;

    public CheckoutGymController(CheckOutGym_Facade facade) {
        this.facade = facade;
    }

    @PostMapping
    public ResponseEntity<CheckOut_Gym_ResponseDTO> createNewCheckOutGym(@RequestBody @Valid CheckOutCreateDTO data,
                                                                         Authentication authentication) {
        try {
            UUID id = UUID.fromString(authentication.getName());
            CheckOut_Gym_ResponseDTO checkOut = facade.create(data, id);
            return ResponseEntity.status(HttpStatus.CREATED).body(checkOut);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<CheckOut_Gym_ResponseDTO>> findAllCheckOutGyms () {
        return ResponseEntity.ok(facade.findAll());
    }


}
