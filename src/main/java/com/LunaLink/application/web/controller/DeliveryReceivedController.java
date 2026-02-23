package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.facades.delivery.DeliveryReceivedFacade;
import com.LunaLink.application.web.dto.DeliveryReceivedDTO.RequestDeliveryReceivedDTO;
import com.LunaLink.application.web.dto.DeliveryReceivedDTO.ResponseDeliveryReceivedDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/lunaLink/deliveryReceived")
public class DeliveryReceivedController {

    private final DeliveryReceivedFacade deliveryReceivedFacade;

    public DeliveryReceivedController(DeliveryReceivedFacade deliveryReceivedFacade) {
        this.deliveryReceivedFacade = deliveryReceivedFacade;
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseDeliveryReceivedDTO> createDeliveryReceived(@RequestBody RequestDeliveryReceivedDTO deliveryReceivedDTO) {
        return ResponseEntity.ok(deliveryReceivedFacade.createDeliveryReceived(deliveryReceivedDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDeliveryReceivedDTO> findDeliveryReceivedById(@PathVariable UUID id) {
        return ResponseEntity.ok(deliveryReceivedFacade.findDeliveryReceivedById(id));
    }




}
