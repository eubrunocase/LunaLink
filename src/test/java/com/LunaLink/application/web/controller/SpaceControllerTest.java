package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.facades.space.SpaceFacade;
import com.LunaLink.application.domain.enums.SpaceType;
import com.LunaLink.application.web.dto.SpaceDTO.SpaceResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpaceControllerTest {

    @Mock
    private SpaceFacade spaceFacade;

    @InjectMocks
    private SpaceController controller;

    @Test
    @DisplayName("Deve listar todos os espaços")
    void listAllSpaces_ShouldReturnList() {
        // Arrange
        List<SpaceResponseDTO> spaces = List.of(
                new SpaceResponseDTO(1L, SpaceType.SALAO_FESTAS),
                new SpaceResponseDTO(2L, SpaceType.CHURRASQUEIRA)
        );
        when(spaceFacade.listAllSpaces()).thenReturn(spaces);

        // Act
        ResponseEntity<List<SpaceResponseDTO>> result = controller.listAllSpaces();

        // Assert
        assertNotNull(result.getBody());
        assertEquals(2, result.getBody().size());
    }
}
