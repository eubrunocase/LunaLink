package com.LunaLink.application.application.facades.users;

import com.LunaLink.application.application.ports.input.UserServicePort;
import com.LunaLink.application.web.dto.UserDTO.RequestUserDTO;
import com.LunaLink.application.web.dto.UserDTO.ResponseUserDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class UserFacade {

    private final UserServicePort userServicePort;

    public UserFacade(UserServicePort userServicePort) {
        this.userServicePort = userServicePort;
    }

    public ResponseUserDTO findResidentByLogin (String login) {
        return userServicePort.findUserByLogin(login);
    }

    public ResponseUserDTO createUser(RequestUserDTO users) {
        return userServicePort.createUser(users);
    }

    public void deleteUser(UUID id) {
        userServicePort.deleteUser(id);
    }

    public ResponseUserDTO updateUser(UUID id, RequestUserDTO users) {
        return userServicePort.updateUser(id, users);
    }

    public ResponseUserDTO findResidentById(UUID id) {
        return userServicePort.findUserById(id);
    }

    public List<ResponseUserDTO> findAllResidents() {
        return userServicePort.findAll();
    }
    
}
