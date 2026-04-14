package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.facades.occurrence.OccurrenceFacade;
import com.LunaLink.application.web.dto.OccurrenceDTO.OccurrenceCreateRequestDTO;
import com.LunaLink.application.web.dto.OccurrenceDTO.OccurrenceResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OccurrenceControllerTest {

    @Mock
    private OccurrenceFacade facade;

    @InjectMocks
    private OccurrenceController controller;

    @Test
    @DisplayName("Deve retornar status 201 ao criar ocorrencia")
    void createOccurrence_ShouldReturnCreated() {
        // Arrange
        OccurrenceCreateRequestDTO dto = new OccurrenceCreateRequestDTO("Barulho", LocalDateTime.now());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test@email.com");

        OccurrenceResponseDTO responseDTO = new OccurrenceResponseDTO(UUID.randomUUID(), "Test", "Barulho", dto.incidentDate(), LocalDateTime.now());
        when(facade.createOccurrence(dto, "test@email.com")).thenReturn(responseDTO);

        // Act
        ResponseEntity<OccurrenceResponseDTO> response = controller.createOccurrence(dto, authentication);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
