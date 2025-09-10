package com.LunaLink.application.web.controller;

import com.LunaLink.application.core.services.businnesRules.ResidentService;
import com.LunaLink.application.core.services.jwtService.TokenService;
import com.LunaLink.application.core.domain.Resident;
import com.LunaLink.application.web.dto.residentDTO.ResidentResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lunaLink/resident")
public class ResidentController {

    private final ResidentService residentService;
    private final TokenService tokenService;

    public ResidentController(ResidentService residentService, TokenService tokenService) {
        this.residentService = residentService;
        this.tokenService = tokenService;
    }

    @PostMapping
    public ResponseEntity<Resident> createResident(@RequestBody Resident resident) {
        System.out.println("RECEBENDO CRIAÇÃO DO RESIDENTE " + resident + "NO CONTROLLLER");
        Resident savedResident = residentService.createResident(resident);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedResident);
    }

    @GetMapping
    public ResponseEntity<List<ResidentResponseDTO>> getAllResidents() {
        return ResponseEntity.ok(residentService.findAllResidents());
    }

    @DeleteMapping("/{id}")
    public void deleteResidentById(@PathVariable Long id) {
        residentService.delete(id);
    }

    @PutMapping("/{id}")
    public Resident updateResident(@PathVariable Long id, @RequestBody Resident resident) {
        resident.setId(id);
        return residentService.save(resident);
    }

    @GetMapping("/profile")
    public ResponseEntity<Resident> findByToken (@RequestHeader("Authorization") String auth) {
        String token = auth.replace("Bearer ", "").trim();
        String login = tokenService.validateToken(token);

        Resident profile = residentService.findResidentByLogin(auth);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResidentResponseDTO> findResidentById(@PathVariable Long id) {
        ResidentResponseDTO resident = residentService.findResidentById(id);
        return ResponseEntity.ok(resident);
    }

}
