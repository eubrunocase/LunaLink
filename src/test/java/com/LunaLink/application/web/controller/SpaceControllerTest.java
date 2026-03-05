package com.LunaLink.application.web.controller;

import com.LunaLink.application.application.service.space.SpaceService;
import com.LunaLink.application.domain.model.space.Space;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpaceControllerTest {

    @Mock
    private SpaceService spaceService;

    @InjectMocks
    private SpaceController controller;

    @Test
    @DisplayName("Deve listar todos os espaços")
    void listAllSpaces_ShouldReturnList() {
        // Arrange
        List<Space> spaces = List.of(new Space(), new Space());
        when(spaceService.listAllReservations()).thenReturn(spaces);

        // Act
        List<Space> result = controller.listAllSpaces();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }
}
