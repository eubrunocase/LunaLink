package com.LunaLink.application.application.ports.input;

import com.LunaLink.application.web.dto.UserDTO.RequestUserDTO;
import com.LunaLink.application.web.dto.UserDTO.ResponseUserDTO;

import java.util.List;
import java.util.UUID;

public interface UserServicePort {
    ResponseUserDTO createUser(RequestUserDTO data);
    ResponseUserDTO findUserById(UUID id);
    ResponseUserDTO findUserByLogin(String login);
    ResponseUserDTO updateUser(UUID id, RequestUserDTO data);
    void deleteUser(UUID id);
    List<ResponseUserDTO> findAll();
}
