package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.facades.space.SpaceFacade;
import com.LunaLink.application.web.dto.SpaceDTO.SpaceResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lunaLink/space")
public class SpaceController {

    private final SpaceFacade spaceFacade;

    public SpaceController(SpaceFacade spaceFacade) {
        this.spaceFacade = spaceFacade;
    }

    @GetMapping
    public ResponseEntity<List<SpaceResponseDTO>> listAllSpaces() {
        return ResponseEntity.ok(spaceFacade.listAllSpaces());
    }

}
