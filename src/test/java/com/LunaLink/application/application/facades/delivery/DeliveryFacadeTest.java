package com.LunaLink.application.application.facades.delivery;

import com.LunaLink.application.application.service.delivery.DeliveryService;
import com.LunaLink.application.web.dto.DeliveryDTO.RequestDeliveryDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryFacadeTest {

    @Mock
    private DeliveryService deliveryService;

    @InjectMocks
    private DeliveryFacade facade;

    @Test
    @DisplayName("Deve criar entrega")
    void createDelivery_ShouldCallService() {
        // Arrange
        RequestDeliveryDTO requestDTO = new RequestDeliveryDTO(UUID.randomUUID(), "PROTO-123", "Discriminação", null, null);

        // Act
        facade.createDelivery(requestDTO);

        // Assert
        verify(deliveryService, times(1)).createDelivery(requestDTO);
    }

    @Test
    @DisplayName("Deve listar todas as entregas")
    void findAllDeliveries_ShouldCallService() {
        // Act
        facade.findAllDeliveries();

        // Assert
        verify(deliveryService, times(1)).findAllDeliveries();
    }

    @Test
    @DisplayName("Deve buscar entrega por ID")
    void findById_ShouldCallService() {
        // Arrange
        UUID id = UUID.randomUUID();

        // Act
        facade.findById(id);

        // Assert
        verify(deliveryService, times(1)).findDeliveryById(id);
    }

    @Test
    @DisplayName("Deve atualizar entrega")
    void updateDelivery_ShouldCallService() {
        // Arrange
        UUID id = UUID.randomUUID();
        RequestDeliveryDTO requestDTO = new RequestDeliveryDTO(UUID.randomUUID(), "PROTO-123", "Discriminação", null, null);

        // Act
        facade.updateDelivery(id, requestDTO);

        // Assert
        verify(deliveryService, times(1)).updateDelivery(id, requestDTO);
    }

    @Test
    @DisplayName("Deve confirmar recebimento")
    void confirmReceipt_ShouldCallService() {
        // Arrange
        UUID id = UUID.randomUUID();
        String pickedUpBy = "Porteiro";

        // Act
        facade.confirmReceipt(id, pickedUpBy);

        // Assert
        verify(deliveryService, times(1)).confirmReceipt(id, pickedUpBy);
    }
}
