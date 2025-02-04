package com.example.LunaLink.controller;

import com.example.LunaLink.model.AuthenticationDTO;
import com.example.LunaLink.model.RegisterDTO;
import com.example.LunaLink.model.Users;
import com.example.LunaLink.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lunaLink/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity login (@RequestBody @Valid AuthenticationDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity register (@RequestBody @Valid RegisterDTO data) {
      if (this.userRepository.findByLogin(data.login()) != null)
          return ResponseEntity.badRequest().build();

      String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
      Users user = new Users(data.login(), encryptedPassword, data.role());

      this.userRepository.save(user);

      return ResponseEntity.ok().build();
    }

}
