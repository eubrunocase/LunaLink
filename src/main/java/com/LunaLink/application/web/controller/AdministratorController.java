package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.facades.administrator.AdministratorFacade;
import com.LunaLink.application.application.service.auth.TokenService;
import com.LunaLink.application.domain.model.administrator.Administrator;
import com.LunaLink.application.web.dto.AdministratorDTO.AdministratorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lunaLink/adm")
public class AdministratorController {

     private final AdministratorFacade facade;
     private final TokenService tokenService;

     public AdministratorController(TokenService tokenService, AdministratorFacade facade) {
         this.tokenService = tokenService;
         this.facade = facade;
     }

     @PostMapping
     public ResponseEntity<Administrator> createAdministrator(@RequestBody Administrator administrator) {
         System.out.println("RECEBENDO CRIAÇÃO DO ADMINISTRADOR " + administrator.getLogin() + "NO CONTROLLLER");
         Administrator savedAdministrator = facade.createAdministrator(administrator);
         return ResponseEntity.status(HttpStatus.CREATED).body(savedAdministrator);
     }

     @GetMapping
     public ResponseEntity<List<AdministratorResponseDTO>> getAllAdministrators() {
         return ResponseEntity.ok(facade.findAllAdm());
     }

     @DeleteMapping("/{id}")
     public void deleteAdministratorById(@PathVariable Long id) {
         facade.deleteAdministrator(id);
     }

     @PutMapping("/{id}")
     public Administrator updateAdministratorById(@PathVariable Long id, @RequestBody Administrator administrator) {
         administrator.setId(id);
         return facade.updateAdministrator(id ,administrator);
     }

    @GetMapping("/profile")
    public ResponseEntity<Administrator> findByToken (@RequestHeader("Authorization") String auth) {
        String token = auth.replace("Bearer ", "").trim();
        String login = tokenService.validateToken(token);

        Administrator profile = facade.findAdmByLogin(auth);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdministratorResponseDTO> findAdmById(@PathVariable Long id) {
         AdministratorResponseDTO adm = facade.findAdmById(id);
         return ResponseEntity.ok(adm);
    }

}
