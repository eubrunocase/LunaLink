package com.LunaLink.application.application.service.occurrence;

import com.LunaLink.application.application.ports.output.OccurrenceRepositoryPort;
import com.LunaLink.application.application.ports.output.UserRepositoryPort;
import com.LunaLink.application.domain.events.occurrenceEvents.OccurrenceCreatedEvent;
import com.LunaLink.application.domain.model.occurrence.Occurrence;
import com.LunaLink.application.domain.model.users.Users;
import com.LunaLink.application.infrastructure.eventPublisher.EventPublisher;
import com.LunaLink.application.web.dto.OccurrenceDTO.OccurrenceCreateRequestDTO;
import com.LunaLink.application.web.dto.OccurrenceDTO.OccurrenceResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OccurrenceServiceTest {

    @Mock
    private OccurrenceRepositoryPort occurrenceRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private OccurrenceService occurrenceService;

    private Users user;

    @BeforeEach
    void setUp() {
        user = new Users();
        user.setId(UUID.randomUUID());
        user.setName("Bruno");
        user.setEmail("bruno@test.com");
    }

    @Test
    @DisplayName("Deve criar ocorrencia com sucesso")
    void createOccurrence_ShouldSucceed() {
        // Arrange
        LocalDateTime incidentDate = LocalDateTime.now().minusHours(1);
        OccurrenceCreateRequestDTO dto = new OccurrenceCreateRequestDTO("Barulho excessivo", incidentDate);
        
        Occurrence savedOccurrence = new Occurrence(user, dto.description(), dto.incidentDate());
        savedOccurrence.setId(UUID.randomUUID());
        savedOccurrence.setCreatedAt(LocalDateTime.now());

        when(userRepositoryPort.findByEmail("bruno@test.com")).thenReturn(user);
        when(occurrenceRepositoryPort.save(any(Occurrence.class))).thenReturn(savedOccurrence);

        // Act
        OccurrenceResponseDTO result = occurrenceService.createOccurrence(dto, "bruno@test.com");

        // Assert
        assertNotNull(result);
        assertEquals("Bruno", result.userName());
        assertEquals("Barulho excessivo", result.description());
        verify(eventPublisher, times(1)).publishEvent(any(OccurrenceCreatedEvent.class));
    }

    @Test
    @DisplayName("Deve falhar ao criar ocorrencia com data no futuro")
    void createOccurrence_ShouldFail_WhenFutureDate() {
        // Arrange
        LocalDateTime futureDate = LocalDateTime.now().plusDays(1);
        OccurrenceCreateRequestDTO dto = new OccurrenceCreateRequestDTO("Barulho excessivo", futureDate);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> occurrenceService.createOccurrence(dto, "bruno@test.com"));
            
        assertEquals("A data do incidente não pode ser no futuro.", exception.getMessage());
        verify(occurrenceRepositoryPort, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}
