package com.example.LunaLink.controller;
import com.example.LunaLink.model.Space;
import com.example.LunaLink.repository.SpaceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/space")
public class SpaceController {

    private final SpaceRepository spaceRepository;

    public SpaceController(SpaceRepository spaceRepository) {
        this.spaceRepository = spaceRepository;
    }

    @PostMapping
    public ResponseEntity<Space> createSpace(@RequestBody Space space) {
        Space savedSpace = spaceRepository.save(space);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedSpace);
    }

    @GetMapping
    public List<Space> getAllSpaces () {
        return spaceRepository.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Space> getSpaceById (@PathVariable long id) {
         return spaceRepository.findById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteSpaceById(@PathVariable long id) {
        spaceRepository.deleteById(id);
    }

    @PutMapping("/{id}")
    public Space AttSpace (@PathVariable long id,@RequestBody Space space) {
       space.setId(id);
       return spaceRepository.save(space);
    }


}
