package com.LunaLink.application.web.controller;

import com.LunaLink.application.core.services.businnesRules.AdministratorService;
import com.LunaLink.application.core.services.jwtService.TokenService;
import com.LunaLink.application.core.domain.Administrator;
import com.LunaLink.application.web.dto.AdministratorDTO.AdministratorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lunaLink/adm")
public class AdministratorController {

     private final AdministratorService administratorService;
     private final TokenService tokenService;

     public AdministratorController(AdministratorService administratorService, TokenService tokenService) {
         this.administratorService = administratorService;
         this.tokenService = tokenService;
     }

     @PostMapping
     public ResponseEntity<Administrator> createAdministrator(@RequestBody Administrator administrator) {
         System.out.println("RECEBENDO CRIAÇÃO DO ADMINISTRADOR " + administrator.getLogin() + "NO CONTROLLLER");
         Administrator savedAdministrator = administratorService.createAdministrator(administrator);
         return ResponseEntity.status(HttpStatus.CREATED).body(savedAdministrator);
     }

     @GetMapping
     public ResponseEntity<List<AdministratorResponseDTO>> getAllAdministrators() {
         return ResponseEntity.ok(administratorService.findAllAdm());
     }

     @DeleteMapping("/{id}")
     public void deleteAdministratorById(@PathVariable Long id) {
         administratorService.delete(id);
     }

     @PutMapping("/{id}")
     public Administrator updateAdministratorById(@PathVariable Long id, @RequestBody Administrator administrator) {
         administrator.setId(id);
         return administratorService.save(administrator);
     }

    @GetMapping("/profile")
    public ResponseEntity<Administrator> findByToken (@RequestHeader("Authorization") String auth) {
        String token = auth.replace("Bearer ", "").trim();
        String login = tokenService.validateToken(token);

        Administrator profile = administratorService.findAdmByLogin(auth);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdministratorResponseDTO> findAdmById(@PathVariable Long id) {
         AdministratorResponseDTO adm = administratorService.findAdmById(id);
         return ResponseEntity.ok(adm);
    }

}
