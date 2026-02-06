package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.service.space.SpaceService;
import com.LunaLink.application.domain.model.space.Space;
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
