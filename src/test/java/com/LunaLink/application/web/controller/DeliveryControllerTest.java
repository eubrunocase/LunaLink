package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.facades.delivery.DeliveryFacade;
import com.LunaLink.application.domain.enums.DeliveryStatus;
import com.LunaLink.application.web.dto.DeliveryDTO.RequestDeliveryDTO;
import com.LunaLink.application.web.dto.DeliveryDTO.ResponseDeliveryDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryControllerTest {

    @Mock
    private DeliveryFacade deliveryFacade;

    @InjectMocks
    private DeliveryController controller;

    @Test
    @DisplayName("Deve listar todas as entregas")
    void findAllDeliveries_ShouldReturnList() {
        // Arrange
        List<ResponseDeliveryDTO> deliveries = List.of(new ResponseDeliveryDTO(UUID.randomUUID(), UUID.randomUUID(), "123", "Discriminação", null, LocalDateTime.now(), null, DeliveryStatus.PENDING, null, null));
        when(deliveryFacade.findAllDeliveries()).thenReturn(deliveries);

        // Act
        ResponseEntity<List<ResponseDeliveryDTO>> response = controller.findAllDeliveries();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("Deve buscar entrega por ID")
    void findById_ShouldReturnDelivery() {
        // Arrange
        UUID id = UUID.randomUUID();
        ResponseDeliveryDTO delivery = new ResponseDeliveryDTO(id, UUID.randomUUID(), "123", "Discriminação", null, LocalDateTime.now(), null, DeliveryStatus.PENDING, null, null);
        when(deliveryFacade.findById(id)).thenReturn(delivery);

        // Act
        ResponseEntity<ResponseDeliveryDTO> response = controller.findById(id);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(id, response.getBody().Id());
    }

    @Test
    @DisplayName("Deve criar entrega")
    void createDelivery_ShouldReturnCreated() {
        // Arrange
        RequestDeliveryDTO request = new RequestDeliveryDTO(UUID.randomUUID(), "123", "Discriminação", null, null);
        ResponseDeliveryDTO created = new ResponseDeliveryDTO(UUID.randomUUID(), request.userId(), request.protocolNumber(), request.discrimination(), null, LocalDateTime.now(), null, DeliveryStatus.PENDING, null, null);
        when(deliveryFacade.createDelivery(request)).thenReturn(created);

        // Act
        ResponseEntity<ResponseDeliveryDTO> response = controller.createDelivery(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(request.protocolNumber(), response.getBody().protocolNumber());
    }

    @Test
    @DisplayName("Deve atualizar entrega")
    void updateDelivery_ShouldReturnUpdated() {
        // Arrange
        UUID id = UUID.randomUUID();
        RequestDeliveryDTO request = new RequestDeliveryDTO(UUID.randomUUID(), "123", "Discriminação", null, null);
        ResponseDeliveryDTO updated = new ResponseDeliveryDTO(id, request.userId(), request.protocolNumber(), request.discrimination(), null, LocalDateTime.now(), null, DeliveryStatus.PENDING, null, null);
        when(deliveryFacade.updateDelivery(id, request)).thenReturn(updated);

        // Act
        ResponseEntity<ResponseDeliveryDTO> response = controller.updateDelivery(id, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(id, response.getBody().Id());
    }

    @Test
    @DisplayName("Deve confirmar recebimento")
    void confirmReceipt_ShouldReturnUpdated() {
        // Arrange
        UUID id = UUID.randomUUID();
        String pickedUpBy = "Porteiro";
        ResponseDeliveryDTO updated = new ResponseDeliveryDTO(id, UUID.randomUUID(), "123", "Discriminação", null, LocalDateTime.now(), null, DeliveryStatus.DELIVERED, LocalDateTime.now(), pickedUpBy);
        when(deliveryFacade.confirmReceipt(id, pickedUpBy)).thenReturn(updated);

        // Act
        ResponseEntity<ResponseDeliveryDTO> response = controller.confirmReceipt(id, pickedUpBy);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(DeliveryStatus.DELIVERED, response.getBody().status());
    }
}
