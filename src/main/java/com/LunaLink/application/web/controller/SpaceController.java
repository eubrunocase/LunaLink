package com.LunaLink.application.web.controller;

import com.LunaLink.application.core.services.businnesRules.SpaceService;
import com.LunaLink.application.core.domain.Space;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lunaLink/space")
public class SpaceController {

    private final SpaceService spaceService;

    public SpaceController(SpaceService spaceService) {
        this.spaceService = spaceService;
    }

    @GetMapping
    public List<Space> listAllSpaces () {
        return spaceService.listAllReservations();
    }

}
