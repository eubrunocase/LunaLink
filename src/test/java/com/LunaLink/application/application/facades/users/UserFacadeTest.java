package com.LunaLink.application.application.facades.users;

import com.LunaLink.application.application.service.User.UserService;
import com.LunaLink.application.domain.enums.UserRoles;
import com.LunaLink.application.web.dto.UserDTO.RequestUserDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserFacadeTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserFacade facade;

    @Test
    @DisplayName("Deve criar usuário")
    void createUser_ShouldCallService() {
        // Arrange
        RequestUserDTO requestDTO = new RequestUserDTO("user", "password", UserRoles.RESIDENT_ROLE);

        // Act
        facade.createUser(requestDTO);

        // Assert
        verify(userService, times(1)).createUser(requestDTO);
    }

    @Test
    @DisplayName("Deve buscar usuário por ID")
    void findResidentById_ShouldCallService() {
        // Arrange
        UUID id = UUID.randomUUID();

        // Act
        facade.findResidentById(id);

        // Assert
        verify(userService, times(1)).findUserById(id);
    }

    @Test
    @DisplayName("Deve listar todos os usuários")
    void findAllResidents_ShouldCallService() {
        // Act
        facade.findAllResidents();

        // Assert
        verify(userService, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve atualizar usuário")
    void updateUser_ShouldCallService() {
        // Arrange
        UUID id = UUID.randomUUID();
        RequestUserDTO requestDTO = new RequestUserDTO("user", "password", UserRoles.RESIDENT_ROLE);

        // Act
        facade.updateUser(id, requestDTO);

        // Assert
        verify(userService, times(1)).updateUser(id, requestDTO);
    }

    @Test
    @DisplayName("Deve deletar usuário")
    void deleteUser_ShouldCallService() {
        // Arrange
        UUID id = UUID.randomUUID();

        // Act
        facade.deleteUser(id);

        // Assert
        verify(userService, times(1)).deleteUser(id);
    }

    @Test
    @DisplayName("Deve listar resumos de usuários")
    void findAllSummaries_ShouldCallService() {
        // Act
        facade.findAllSummaries();

        // Assert
        verify(userService, times(1)).findAllSummaries();
    }
}
