package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.facades.delivery.DeliveryFacade;
import com.LunaLink.application.infrastructure.mapper.delivery.DeliveryMapper;
import com.LunaLink.application.web.dto.DeliveryDTO.RequestDeliveryDTO;
import com.LunaLink.application.web.dto.DeliveryDTO.ResponseDeliveryDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/lunalink/delivery")
public class DeliveryController {

    private final DeliveryFacade deliveryFacade;
    private final DeliveryMapper deliveryMapper;

    public DeliveryController(DeliveryFacade  deliveryFacade, DeliveryMapper deliveryMapper) {
        this.deliveryFacade =  deliveryFacade;
        this.deliveryMapper = deliveryMapper;
    }

    @GetMapping("/findAll")
    public ResponseEntity<List<ResponseDeliveryDTO>> findAllDeliveries() {
        List<ResponseDeliveryDTO> responseDeliveryDTOs = deliveryFacade.findAllDeliveries();
        return ResponseEntity.ok(responseDeliveryDTOs);
    }

    @GetMapping("/find/{id}")
    public ResponseEntity<ResponseDeliveryDTO> findById(@PathVariable UUID id) {
        ResponseDeliveryDTO responseDeliveryDTO = deliveryFacade.findById(id);
        return ResponseEntity.ok(responseDeliveryDTO);
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseDeliveryDTO> createDelivery(@RequestBody RequestDeliveryDTO deliveryDTO) {
        ResponseDeliveryDTO responseDeliveryDTO = deliveryFacade.createDelivery(deliveryDTO);
        return ResponseEntity.ok(responseDeliveryDTO);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ResponseDeliveryDTO> updateDelivery(@PathVariable UUID id ,@RequestBody RequestDeliveryDTO deliveryDTO) {
        ResponseDeliveryDTO responseDeliveryDTO = deliveryFacade.updateDelivery(id, deliveryDTO);
        return ResponseEntity.ok(responseDeliveryDTO);
    }

}
