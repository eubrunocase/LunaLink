package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.businnesRules.SpaceService;
import com.LunaLink.application.core.Space;
import com.LunaLink.application.core.enums.SpaceType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lunaLink/space")
public class SpaceController {

    private final SpaceService spaceService;

    public SpaceController(SpaceService spaceService) {
        this.spaceService = spaceService;
    }

     @PostMapping
    public ResponseEntity<Space> createNewSpace (@RequestBody SpaceType spaceType) throws Exception{
         try {
             Space spaceCreated = spaceService.createSpace(spaceType);
             return ResponseEntity.status(HttpStatus.CREATED).body(spaceCreated);
         } catch (Exception e) {
             return ResponseEntity.badRequest().build();
         }
    }



}
