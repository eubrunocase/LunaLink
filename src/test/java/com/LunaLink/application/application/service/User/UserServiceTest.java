package com.LunaLink.application.application.service.User;

import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.domain.enums.UserRoles;
import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.infrastructure.mapper.User.UserMapper;
import com.LunaLink.application.web.dto.UserDTO.RequestUserDTO;
import com.LunaLink.application.web.dto.UserDTO.ResponseUserDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Deve criar usuário com senha criptografada")
    void createUser_ShouldEncodePassword_WhenValidData() {
        // Arrange
        RequestUserDTO request = new RequestUserDTO("user", "password", UserRoles.RESIDENT_ROLE);
        Users user = new Users("user", "encodedPassword", UserRoles.RESIDENT_ROLE);
        ResponseUserDTO expectedResponse = new ResponseUserDTO(UUID.randomUUID(), "user", UserRoles.RESIDENT_ROLE, null);

        when(encoder.encode("password")).thenReturn("encodedPassword");
        when(userRepositoryPort.save(any(Users.class))).thenReturn(user);
        when(userMapper.toDTO(any(Users.class))).thenReturn(expectedResponse);

        // Act
        ResponseUserDTO result = userService.createUser(request);

        // Assert
        assertNotNull(result);
        verify(encoder).encode("password");
        verify(userRepositoryPort).save(any(Users.class));
    }

    @Test
    @DisplayName("Deve buscar usuário por login com sucesso")
    void findUserByLogin_ShouldReturnDTO_WhenFound() {
        // Arrange
        String login = "testUser";
        Users user = new Users(login, "pass", UserRoles.RESIDENT_ROLE);
        ResponseUserDTO expectedResponse = new ResponseUserDTO(UUID.randomUUID(), login, UserRoles.RESIDENT_ROLE, null);

        when(userRepositoryPort.findByLogin(login)).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(expectedResponse);

        // Act
        ResponseUserDTO result = userService.findUserByLogin(login);

        // Assert
        assertNotNull(result);
        assertEquals(login, result.login());
    }

    @Test
    @DisplayName("Deve atualizar usuário com sucesso")
    void updateUser_ShouldUpdateFields_WhenFound() {
        // Arrange
        UUID userId = UUID.randomUUID();
        RequestUserDTO request = new RequestUserDTO("newLogin", "newPassword", UserRoles.ADMIN_ROLE);
        Users existingUser = new Users("oldLogin", "oldPassword", UserRoles.RESIDENT_ROLE);
        existingUser.setId(userId);

        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(existingUser));
        when(encoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(userRepositoryPort.save(existingUser)).thenReturn(existingUser);
        
        ResponseUserDTO expectedResponse = new ResponseUserDTO(userId, "newLogin", UserRoles.ADMIN_ROLE, null);
        when(userMapper.toDTO(existingUser)).thenReturn(expectedResponse);

        // Act
        ResponseUserDTO result = userService.updateUser(userId, request);

        // Assert
        assertNotNull(result);
        assertEquals("newLogin", existingUser.getLogin());
        assertEquals("encodedNewPassword", existingUser.getPassword());
        assertEquals(UserRoles.ADMIN_ROLE, existingUser.getRole());
    }
}
