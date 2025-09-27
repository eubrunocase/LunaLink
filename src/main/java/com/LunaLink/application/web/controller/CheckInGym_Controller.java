package com.LunaLink.application.web.controller;

import com.LunaLink.application.core.services.businnesRules.facades.CheckInGym_Facade;
import com.LunaLink.application.web.dto.checkIn_gym_DTO.CheckIn_Gym_RequestDTO;
import com.LunaLink.application.web.dto.checkIn_gym_DTO.CheckIn_Gym_ResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/lunaLink/checkInGym")
public class CheckInGym_Controller {


    private final CheckInGym_Facade facade;

    public CheckInGym_Controller(CheckInGym_Facade facade) {
        this.facade = facade;
    }

    @PostMapping
    public ResponseEntity<CheckIn_Gym_ResponseDTO> createNewCheckin (@RequestBody @Valid CheckIn_Gym_RequestDTO data) {
        try {

            CheckIn_Gym_ResponseDTO checkin = facade.createCheckIn(data);
            return ResponseEntity.status(HttpStatus.CREATED).body(checkin);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CheckIn_Gym_ResponseDTO> findCheckinById(@RequestBody @Valid UUID id) {
        CheckIn_Gym_ResponseDTO data = facade.findCheckIn_GymById(id);
        return ResponseEntity.ok(data);
    }

    @GetMapping
    public ResponseEntity<List<CheckIn_Gym_ResponseDTO>> findAllCheckins () {
        return ResponseEntity.ok(facade.findAllCheckIn_Gyms());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCheckinById(@RequestBody @Valid UUID id) {
        facade.deleteCheckIn_Gym(id);
        return ResponseEntity.noContent().build();
    }

}
