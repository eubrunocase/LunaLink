package com.LunaLink.application.web.controller;

import com.LunaLink.application.core.services.businnesRules.facades.ResidentFacade;
import com.LunaLink.application.core.services.jwtService.TokenService;
import com.LunaLink.application.core.domain.Resident;
import com.LunaLink.application.web.dto.residentDTO.ResidentResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/lunaLink/resident")
public class ResidentController {

    private final ResidentFacade facade;
    private final TokenService tokenService;

    public ResidentController(TokenService tokenService, ResidentFacade facade) {
        this.tokenService = tokenService;
        this.facade = facade;
    }

    @PostMapping
    public ResponseEntity<Resident> createResident(@RequestBody Resident resident) {
        System.out.println("RECEBENDO CRIAÇÃO DO RESIDENTE " + resident + "NO CONTROLLLER");
        Resident savedResident = facade.createResident(resident);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedResident);
    }

    @GetMapping
    public ResponseEntity<List<ResidentResponseDTO>> getAllResidents() {
        return ResponseEntity.ok(facade.findAllResidents());
    }

    @DeleteMapping("/{id}")
    public void deleteResidentById(@PathVariable Long id) {
        facade.deleteResident(id);
    }

    @PutMapping("/{id}")
    public Resident updateResident(@PathVariable Long id, @RequestBody Resident resident) {
        resident.setId(id);
        return facade.createResident(resident);
    }

    @GetMapping("/profile")
    public ResponseEntity<Resident> findByToken (@RequestHeader("Authorization") String auth) {
        String token = auth.replace("Bearer ", "").trim();
        String login = tokenService.validateToken(token);

        Resident profile = facade.findResidentByLogin(auth);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResidentResponseDTO> findResidentById(@PathVariable Long id) {
        ResidentResponseDTO resident = facade.findResidentById(id);
        return ResponseEntity.ok(resident);
    }

}
