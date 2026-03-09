package com.LunaLink.application.application.service.User;

import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.domain.enums.UserRoles;
import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.infrastructure.mapper.User.UserMapper;
import com.LunaLink.application.web.dto.UserDTO.RequestUserDTO;
import com.LunaLink.application.web.dto.UserDTO.ResponseUserDTO;
import com.LunaLink.application.web.dto.UserDTO.UserSummaryDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
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
        RequestUserDTO request = new RequestUserDTO("User Name", "101", "user@email.com", "password", UserRoles.RESIDENT_ROLE);
        Users user = new Users("User Name", "101", "user@email.com", "encodedPassword", UserRoles.RESIDENT_ROLE);
        ResponseUserDTO expectedResponse = new ResponseUserDTO(UUID.randomUUID(), "User Name", "101", "user@email.com", UserRoles.RESIDENT_ROLE, null);

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
    @DisplayName("Deve buscar usuário por email com sucesso")
    void findUserByEmail_ShouldReturnDTO_WhenFound() {
        // Arrange
        String email = "testUser@email.com";
        Users user = new Users("Test User", "101", email, "pass", UserRoles.RESIDENT_ROLE);
        ResponseUserDTO expectedResponse = new ResponseUserDTO(UUID.randomUUID(), "Test User", "101", email, UserRoles.RESIDENT_ROLE, null);

        when(userRepositoryPort.findByEmail(email)).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(expectedResponse);

        // Act
        ResponseUserDTO result = userService.findUserByEmail(email);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.email());
    }

    @Test
    @DisplayName("Deve atualizar usuário com sucesso")
    void updateUser_ShouldUpdateFields_WhenFound() {
        // Arrange
        UUID userId = UUID.randomUUID();
        RequestUserDTO request = new RequestUserDTO("New Name", "102", "newLogin@email.com", "newPassword", UserRoles.ADMIN_ROLE);
        Users existingUser = new Users("Old Name", "101", "oldLogin@email.com", "oldPassword", UserRoles.RESIDENT_ROLE);
        existingUser.setId(userId);

        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(existingUser));
        when(encoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(userRepositoryPort.save(existingUser)).thenReturn(existingUser);
        
        ResponseUserDTO expectedResponse = new ResponseUserDTO(userId, "New Name", "102", "newLogin@email.com", UserRoles.ADMIN_ROLE, null);
        when(userMapper.toDTO(existingUser)).thenReturn(expectedResponse);

        // Act
        ResponseUserDTO result = userService.updateUser(userId, request);

        // Assert
        assertNotNull(result);
        assertEquals("New Name", existingUser.getName());
        assertEquals("102", existingUser.getApartment());
        assertEquals("newLogin@email.com", existingUser.getEmail());
        assertEquals("encodedNewPassword", existingUser.getPassword());
        assertEquals(UserRoles.ADMIN_ROLE, existingUser.getRole());
    }

    @Test
    @DisplayName("Deve listar resumos de usuários")
    void findAllSummaries_ShouldReturnList() {
        // Arrange
        List<Users> users = List.of(new Users("User", "101", "user@email.com", "pass", UserRoles.RESIDENT_ROLE));
        List<UserSummaryDTO> summaries = List.of(new UserSummaryDTO(UUID.randomUUID(), "User", "101", "user@email.com"));
        
        when(userRepositoryPort.findAll()).thenReturn(users);
        when(userMapper.toSummaryDTOList(users)).thenReturn(summaries);

        // Act
        List<UserSummaryDTO> result = userService.findAllSummaries();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("user@email.com", result.get(0).email());
        assertEquals("101", result.get(0).apartment());
    }
}
