package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.facades.users.UserFacade;
import com.LunaLink.application.web.dto.UserDTO.RequestUserDTO;
import com.LunaLink.application.web.dto.UserDTO.ResponseUserDTO;
import com.LunaLink.application.web.mapperImpl.UserMapperImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/lunaLink/users")
public class UsersController {

    private final UserFacade userFacade;
    private final UserMapperImpl userMapper = new UserMapperImpl();

    public UsersController(UserFacade userFacade) {
        this.userFacade = userFacade;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseUserDTO> findUserById(@PathVariable UUID id) {
        ResponseUserDTO user = userFacade.findResidentById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseUserDTO> createUser(@RequestBody RequestUserDTO users) {
        return ResponseEntity.ok(userFacade.createUser(users));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ResponseUserDTO> updateUser(@PathVariable UUID id,@RequestBody RequestUserDTO users) {
        return ResponseEntity.ok(userFacade.updateUser(id, users));
    }

    @DeleteMapping("/delete/{id}")
    public void deleteUser(UUID id) {
        userFacade.deleteUser(id);
    }

}
