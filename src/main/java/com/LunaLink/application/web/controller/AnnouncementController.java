package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.facades.announcement.AnnouncementFacade;
import com.LunaLink.application.web.dto.AnnouncementDTO.RequestAnnouncementDTO;
import com.LunaLink.application.web.dto.AnnouncementDTO.ResponseAnnouncementDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/lunaLink/announcements")
public class AnnouncementController {

    private final AnnouncementFacade announcementFacade;

    public AnnouncementController(AnnouncementFacade announcementFacade) {
        this.announcementFacade = announcementFacade;
    }

    @GetMapping("/findAll")
    public ResponseEntity<List<ResponseAnnouncementDTO>> findAllAnnouncements() {
        List<ResponseAnnouncementDTO> announcements = announcementFacade.findAllAnnouncements();
        return ResponseEntity.ok(announcements);
    }

    @GetMapping("/find/{id}")
    public ResponseEntity<ResponseAnnouncementDTO> findAnnouncementById(@PathVariable UUID id) {
        ResponseAnnouncementDTO announcement = announcementFacade.findAnnouncementById(id);
        return ResponseEntity.ok(announcement);
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseAnnouncementDTO> createAnnouncement(@RequestBody RequestAnnouncementDTO announcementDTO) {
        ResponseAnnouncementDTO response = announcementFacade.createAnnouncement(announcementDTO);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ResponseAnnouncementDTO> updateAnnouncement(@PathVariable UUID id, @RequestBody RequestAnnouncementDTO announcementDTO) {
        ResponseAnnouncementDTO response = announcementFacade.updateAnnouncement(id, announcementDTO);
        return ResponseEntity.ok(response);
    }

}
