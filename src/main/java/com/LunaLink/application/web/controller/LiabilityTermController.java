package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.ports.input.LiabilityTermServicePort;
import com.LunaLink.application.application.ports.input.UserServicePort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/lunaLink/reservations")
public class LiabilityTermController {

    private final LiabilityTermServicePort liabilityTermServicePort;
    private final UserServicePort userServicePort;

    public LiabilityTermController(LiabilityTermServicePort liabilityTermServicePort,
                                   UserServicePort userServicePort) {
        this.liabilityTermServicePort = liabilityTermServicePort;
        this.userServicePort = userServicePort;
    }

    @PostMapping("/{id}/liability-term/sign")
    public ResponseEntity<Void> signLiabilityTerm(
            @PathVariable UUID id,
            Authentication authentication) {

        String email = authentication.getName();
        UUID residentId = userServicePort.findUserByEmail(email).id();

        liabilityTermServicePort.signTerm(id, residentId);

        return ResponseEntity.ok().build();
    }
}
