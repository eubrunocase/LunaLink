package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.facades.occurrence.OccurrenceFacade;
import com.LunaLink.application.web.dto.OccurrenceDTO.OccurrenceCreateRequestDTO;
import com.LunaLink.application.web.dto.OccurrenceDTO.OccurrenceResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/lunaLink/occurrences")
public class OccurrenceController {

    private final OccurrenceFacade occurrenceFacade;

    public OccurrenceController(OccurrenceFacade occurrenceFacade) {
        this.occurrenceFacade = occurrenceFacade;
    }

    @PostMapping
    public ResponseEntity<OccurrenceResponseDTO> createOccurrence(
            @Valid @RequestBody OccurrenceCreateRequestDTO dto,
            Authentication authentication) {
        String userEmail = authentication.getName();
        OccurrenceResponseDTO response = occurrenceFacade.createOccurrence(dto, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/findAll")
    public ResponseEntity<List<OccurrenceResponseDTO>> findAll(Authentication authentication) {
        List<OccurrenceResponseDTO> occurrences = occurrenceFacade.findAllOcurrences(authentication.getName());
        return ResponseEntity.ok(occurrences);
    }

    @GetMapping("/find/{uuid}")
    public ResponseEntity<OccurrenceResponseDTO> findById(@PathVariable UUID uuid, Authentication authentication) {
        OccurrenceResponseDTO occurrence = occurrenceFacade.findOccurrenceById(uuid, authentication.getName());
        return ResponseEntity.ok(occurrence);
    }


    @DeleteMapping("/delete/{uuid}")
    public ResponseEntity<Void> deleteOccurrence(@PathVariable UUID uuid, Authentication authentication) {
        occurrenceFacade.deleteOccurrence(uuid, authentication.getName());
        return ResponseEntity.noContent().build();
    }

}
