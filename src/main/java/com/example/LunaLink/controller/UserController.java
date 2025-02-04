package com.example.LunaLink.controller;
import com.example.LunaLink.model.Users;
import com.example.LunaLink.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/lunaLink/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<Users> createUser(@RequestBody Users Users) {
        Users savedUsers = userRepository.save(Users);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUsers);
    }

    @GetMapping
    public List<Users> getAllUsers () {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Users> getUsersById (@PathVariable long id) {
        return userRepository.findById(id);
    }

    @DeleteMapping("{id}")
    public void deleteUserById(@PathVariable long id) {
        userRepository.deleteById(id);
    }

    @PutMapping("/{id}")
    public Users attUser (@PathVariable long id, @RequestBody Users users) {
        users.setId(id);
        return userRepository.save(users);
    }


}
