package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.facades.users.UserFacade;
import com.LunaLink.application.domain.enums.UserRoles;
import com.LunaLink.application.web.dto.UserDTO.RequestUserDTO;
import com.LunaLink.application.web.dto.UserDTO.ResponseUserDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsersControllerTest {

    @Mock
    private UserFacade userFacade;

    @InjectMocks
    private UsersController controller;

    @Test
    @DisplayName("Deve buscar usuário por ID")
    void findUserById_ShouldReturnUser() {
        // Arrange
        UUID id = UUID.randomUUID();
        ResponseUserDTO user = new ResponseUserDTO(id, "user", UserRoles.RESIDENT_ROLE, null);
        when(userFacade.findResidentById(id)).thenReturn(user);

        // Act
        ResponseEntity<ResponseUserDTO> response = controller.findUserById(id);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(id, response.getBody().id());
    }

    @Test
    @DisplayName("Deve listar todos os usuários")
    void findAllUsers_ShouldReturnList() {
        // Arrange
        List<ResponseUserDTO> users = List.of(new ResponseUserDTO(UUID.randomUUID(), "user", UserRoles.RESIDENT_ROLE, null));
        when(userFacade.findAllResidents()).thenReturn(users);

        // Act
        ResponseEntity<List<ResponseUserDTO>> response = controller.findAllUsers();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("Deve criar usuário")
    void createUser_ShouldReturnCreated() {
        // Arrange
        RequestUserDTO request = new RequestUserDTO("user", "password", UserRoles.RESIDENT_ROLE);
        ResponseUserDTO created = new ResponseUserDTO(UUID.randomUUID(), "user", UserRoles.RESIDENT_ROLE, null);
        when(userFacade.createUser(request)).thenReturn(created);

        // Act
        ResponseEntity<ResponseUserDTO> response = controller.createUser(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("user", response.getBody().login());
    }

    @Test
    @DisplayName("Deve atualizar usuário")
    void updateUser_ShouldReturnUpdated() {
        // Arrange
        UUID id = UUID.randomUUID();
        RequestUserDTO request = new RequestUserDTO("user", "password", UserRoles.RESIDENT_ROLE);
        ResponseUserDTO updated = new ResponseUserDTO(id, "user", UserRoles.RESIDENT_ROLE, null);
        when(userFacade.updateUser(id, request)).thenReturn(updated);

        // Act
        ResponseEntity<ResponseUserDTO> response = controller.updateUser(id, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(id, response.getBody().id());
    }

    @Test
    @DisplayName("Deve deletar usuário")
    void deleteUser_ShouldReturnNoContent() {
        // Arrange
        UUID id = UUID.randomUUID();

        // Act
        controller.deleteUser(id);

        // Assert
        verify(userFacade, times(1)).deleteUser(id);
    }
}
