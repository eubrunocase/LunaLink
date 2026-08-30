package com.LunaLink.application.application.service.delivery;

import com.LunaLink.application.application.ports.input.StorageService;
import com.LunaLink.application.application.ports.output.DeliveryRepositoryPort;
import com.LunaLink.application.domain.enums.DeliveryStatus;
import com.LunaLink.application.domain.events.deliveryEvents.DeliveryCreatedEvent;
import com.LunaLink.application.domain.model.delivery.Delivery;
import com.LunaLink.application.infrastructure.eventPublisher.EventPublisher;
import com.LunaLink.application.infrastructure.mapper.delivery.DeliveryMapper;
import com.LunaLink.application.web.dto.DeliveryDTO.RequestDeliveryDTO;
import com.LunaLink.application.web.dto.DeliveryDTO.ResponseDeliveryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryRepositoryPort repository;

    @Mock
    private DeliveryMapper mapper;

    @Mock
    private EventPublisher publisher;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private DeliveryService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "uploadExpirationMinutes", 15L);
        ReflectionTestUtils.setField(service, "downloadExpirationMinutes", 15L);
    }

    @Test
    @DisplayName("Deve criar uma entrega com sucesso e publicar evento")
    void createDelivery_ShouldReturnDTO_WhenValidData() {
        // Arrange
        UUID userId = UUID.randomUUID();
        RequestDeliveryDTO request = new RequestDeliveryDTO(userId, "PROTO-123", "Pacote pequeno", null, null);
        
        Delivery deliveryEntity = new Delivery(userId, "PROTO-123", "Pacote pequeno", null, null);
        // Simulando ID gerado pelo banco
        try {
            java.lang.reflect.Field idField = Delivery.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(deliveryEntity, UUID.randomUUID());
        } catch (Exception e) {
            fail("Erro ao setar ID via reflection");
        }

        ResponseDeliveryDTO expectedResponse = new ResponseDeliveryDTO(
                deliveryEntity.getId(), userId, "PROTO-123", "Pacote pequeno", null, LocalDateTime.now(), "system", null, 
                DeliveryStatus.PENDING, null, null
        );

        when(mapper.toEntity(request)).thenReturn(deliveryEntity);
        when(repository.save(any(Delivery.class))).thenReturn(deliveryEntity);
        when(mapper.toDTO(deliveryEntity)).thenReturn(expectedResponse);

        // Act
        ResponseDeliveryDTO result = service.createDelivery(request);

        // Assert
        assertNotNull(result);
        assertEquals("PROTO-123", result.protocolNumber());
        assertEquals("Pacote pequeno", result.discrimination());
        
        // Verifica se o repositório foi chamado
        verify(repository, times(1)).save(any(Delivery.class));
        
        // Verifica se o evento foi publicado
        verify(publisher, times(1)).publishEvent(any(DeliveryCreatedEvent.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar entrega nula")
    void createDelivery_ShouldThrowException_WhenNull() {
        assertThrows(RuntimeException.class, () -> service.createDelivery(null));
    }

    @Test
    @DisplayName("Deve confirmar recebimento com sucesso")
    void confirmReceipt_ShouldUpdateStatus_WhenPending() {
        // Arrange
        UUID deliveryId = UUID.randomUUID();
        String pickedUpBy = "Porteiro";
        
        Delivery delivery = new Delivery();
        delivery.setStatus(DeliveryStatus.PENDING);
        
        ResponseDeliveryDTO expectedResponse = new ResponseDeliveryDTO(
                deliveryId, UUID.randomUUID(), "123", "Discriminação", null, LocalDateTime.now(), "system", null,
                DeliveryStatus.DELIVERED, LocalDateTime.now(), pickedUpBy
        );

        when(repository.findDeliveryById(deliveryId)).thenReturn(delivery);
        when(repository.save(any(Delivery.class))).thenReturn(delivery);
        when(mapper.toDTO(any(Delivery.class))).thenReturn(expectedResponse);

        // Act
        ResponseDeliveryDTO result = service.confirmReceipt(deliveryId, pickedUpBy);

        // Assert
        assertNotNull(result);
        assertEquals(DeliveryStatus.DELIVERED, result.status());
        assertEquals(pickedUpBy, result.pickedUpBy());
        
        // Verifica se o status foi alterado na entidade
        assertEquals(DeliveryStatus.DELIVERED, delivery.getStatus());
        assertNotNull(delivery.getDeliveredAt());
    }

    @Test
    @DisplayName("Deve lançar exceção ao confirmar entrega já entregue")
    void confirmReceipt_ShouldThrowException_WhenAlreadyDelivered() {
        // Arrange
        UUID deliveryId = UUID.randomUUID();
        Delivery delivery = new Delivery();
        delivery.setStatus(DeliveryStatus.DELIVERED);
        delivery.setDeliveredAt(LocalDateTime.now());

        when(repository.findDeliveryById(deliveryId)).thenReturn(delivery);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> service.confirmReceipt(deliveryId, "Alguém"));
            
        assertTrue(exception.getMessage().contains("já foi entregue"));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao confirmar entrega inexistente")
    void confirmReceipt_ShouldThrowException_WhenNotFound() {
        // Arrange
        UUID deliveryId = UUID.randomUUID();
        when(repository.findDeliveryById(deliveryId)).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> service.confirmReceipt(deliveryId, "Alguém"));
            
        assertEquals("Encomenda não encontrada", exception.getMessage());
    }

    @Test
    @DisplayName("Deve gerar dados de upload com key no formato correto")
    void generateUploadData_ShouldReturnUploadUrlAndKey() {
        UUID userId = UUID.randomUUID();
        String fileName = "comprovante.jpg";
        String expectedUploadUrl = "http://minio:9000/lunalink/upload-url?X-Amz-Algorithm=...";

        when(storageService.generateUploadUrl(any(String.class), any(Duration.class)))
                .thenReturn(expectedUploadUrl);

        Map<String, String> result = service.generateUploadData(userId, fileName);

        assertNotNull(result);
        assertTrue(result.containsKey("uploadUrl"));
        assertTrue(result.containsKey("key"));
        assertEquals(expectedUploadUrl, result.get("uploadUrl"));
        assertTrue(result.get("key").startsWith("encomendas/" + userId + "/"));
        assertTrue(result.get("key").endsWith("-" + fileName));

        verify(storageService).generateUploadUrl(
                eq(result.get("key")),
                eq(Duration.ofMinutes(15))
        );
    }

    @Test
    @DisplayName("Deve gerar key com UUID aleatório no nome do arquivo")
    void generateUploadData_ShouldGenerateUniqueKey() {
        UUID userId = UUID.randomUUID();
        String fileName = "nota-fiscal.pdf";

        when(storageService.generateUploadUrl(any(String.class), any(Duration.class)))
                .thenReturn("http://url");

        Map<String, String> result1 = service.generateUploadData(userId, fileName);
        Map<String, String> result2 = service.generateUploadData(userId, fileName);

        assertNotEquals(result1.get("key"), result2.get("key"));
    }

    @Test
    @DisplayName("Deve gerar URL de download usando voucherKey da encomenda")
    void generateDownloadUrl_ShouldReturnUrl_WhenDeliveryExists() {
        UUID deliveryId = UUID.randomUUID();
        String voucherKey = "encomendas/user-uuid/abc-comprovante.jpg";
        String expectedDownloadUrl = "http://minio:9000/lunalink/download-url?X-Amz-Algorithm=...";

        Delivery delivery = new Delivery();
        delivery.setVoucherKey(voucherKey);

        when(repository.findDeliveryById(deliveryId)).thenReturn(delivery);
        when(storageService.generateDownloadUrl(eq(voucherKey), any(Duration.class)))
                .thenReturn(expectedDownloadUrl);

        String result = service.generateDownloadUrl(deliveryId);

        assertNotNull(result);
        assertEquals(expectedDownloadUrl, result);

        verify(storageService).generateDownloadUrl(
                eq(voucherKey),
                eq(Duration.ofMinutes(15))
        );
    }

    @Test
    @DisplayName("Deve lançar exceção ao gerar download URL para encomenda inexistente")
    void generateDownloadUrl_ShouldThrowException_WhenDeliveryNotFound() {
        UUID deliveryId = UUID.randomUUID();

        when(repository.findDeliveryById(deliveryId)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> service.generateDownloadUrl(deliveryId));

        assertEquals("Encomenda não encontrada", exception.getMessage());
        verify(storageService, never()).generateDownloadUrl(any(), any());
    }
}
