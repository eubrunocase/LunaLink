package com.LunaLink.application.application.service.User;

import com.LunaLink.application.application.ports.input.UserServicePort;
import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.infrastructure.mapper.User.UserMapper;
import com.LunaLink.application.web.dto.UserDTO.RequestUserDTO;
import com.LunaLink.application.web.dto.UserDTO.ResponseUserDTO;
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
        if(data == null) throw new RuntimeException("MÉTODO CREATE USER DE UserService: User não pode ser nulo.");
        Users user = new Users(data.login(), encoder.encode(data.password()), data.role());
        userRepositoryPort.save(user);
        return userMapper.toDTO(user);

    }

    @Override
    public ResponseUserDTO findUserById(UUID id) {
        Optional<Users> user = userRepositoryPort.findById(id);
        return user.map(userMapper::toDTO).orElse(null);
    }

    @Override
    public ResponseUserDTO findUserByLogin(String login) {
        Users user = userRepositoryPort.findByLogin(login);
        return userMapper.toDTO(user);
    }

    @Override
    public ResponseUserDTO updateUser(UUID id, RequestUserDTO data) {
        Users userForUpdate = userRepositoryPort.findById(id).get();
        userForUpdate.setLogin(data.login());
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
        return List.of();
    }

}
