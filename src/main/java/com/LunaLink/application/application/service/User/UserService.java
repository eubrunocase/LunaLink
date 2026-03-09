package com.LunaLink.application.application.service.User;

import com.LunaLink.application.application.ports.input.UserServicePort;
import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.infrastructure.mapper.User.UserMapper;
import com.LunaLink.application.web.dto.UserDTO.RequestUserDTO;
import com.LunaLink.application.web.dto.UserDTO.ResponseUserDTO;
import com.LunaLink.application.web.dto.UserDTO.UserSummaryDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserServicePort {

    private final UserRepositoryPort userRepositoryPort;
    private final UserMapper userMapper;
    private final PasswordEncoder encoder;

    public UserService(UserMapper userMapper,
                       UserRepositoryPort userRepositoryPort,
                       PasswordEncoder encoder) {
        this.userMapper = userMapper;
        this.userRepositoryPort = userRepositoryPort;
        this.encoder = encoder;
    }

    @Override
    public ResponseUserDTO createUser(RequestUserDTO data) {
        try {
            if(data == null) throw new RuntimeException("MÉTODO CREATE USER DE UserService: User não pode ser nulo.");
            
            System.out.println("Criando usuário: " + data.email());
            
            String encodedPassword = encoder.encode(data.password());
            Users user = new Users(data.name(), data.apartment(), data.email(), encodedPassword, data.role());
            
            System.out.println("Salvando usuário no banco...");
            userRepositoryPort.save(user);
            
            System.out.println("Usuário salvo. Mapeando para DTO...");
            return userMapper.toDTO(user);
        } catch (Exception e) {
            System.err.println("Erro ao criar usuário: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-lança para o controller pegar
        }
    }

    @Override
    public ResponseUserDTO findUserById(UUID id) {
        Optional<Users> user = userRepositoryPort.findById(id);
        return user.map(userMapper::toDTO).orElse(null);
    }

    @Override
    public ResponseUserDTO findUserByEmail(String email) {
        Users user = userRepositoryPort.findByEmail(email);
        return userMapper.toDTO(user);
    }

    @Override
    public ResponseUserDTO updateUser(UUID id, RequestUserDTO data) {
        Users userForUpdate = userRepositoryPort.findById(id).get();
        userForUpdate.setName(data.name());
        userForUpdate.setApartment(data.apartment());
        userForUpdate.setEmail(data.email());
        userForUpdate.setPassword(encoder.encode(data.password()));
        userForUpdate.setRole(data.role());
        userRepositoryPort.save(userForUpdate);
            return userMapper.toDTO(userForUpdate);
    }

    @Override
    public void deleteUser(UUID id) {
        userRepositoryPort.deleteById(id);
    }

    @Override
    public List<ResponseUserDTO> findAll() {
        List<Users> users = userRepositoryPort.findAll();
        return userMapper.toDTOList(users);
    }

    @Override
    public List<UserSummaryDTO> findAllSummaries() {
        List<Users> users = userRepositoryPort.findAll();
        return userMapper.toSummaryDTOList(users);
    }

}
