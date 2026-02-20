package com.LunaLink.application.infrastructure.mapper.User;

import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.web.dto.UserDTO.ResponseUserDTO;

import org.springframework.stereotype.Component;

import java.util.List;


public interface UserMapper {

    ResponseUserDTO toDTO(Users user);
    List<ResponseUserDTO> toDTOList(List<Users> users);
}
