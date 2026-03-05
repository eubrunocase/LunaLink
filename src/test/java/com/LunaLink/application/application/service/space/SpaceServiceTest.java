package com.LunaLink.application.application.service.space;

import com.LunaLink.application.domain.model.space.Space;
import com.LunaLink.application.infrastructure.repository.space.SpaceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpaceServiceTest {

    @Mock
    private SpaceRepository spaceRepository;

    @InjectMocks
    private SpaceService service;

    @Test
    @DisplayName("Deve listar todos os espaços corretamente")
    void listAllSpaces_ShouldReturnList_WhenFound() {
        // Arrange
        List<Space> spaces = List.of(new Space(), new Space());
        when(spaceRepository.findAll()).thenReturn(spaces);

        // Act
        List<Space> result = service.listAllReservations();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Deve buscar espaço por ID corretamente")
    void findSpaceById_ShouldReturnSpace_WhenFound() {
        // Arrange
        Long spaceId = 1L;
        Space space = new Space();
        space.setId(spaceId);
        when(spaceRepository.findSpaceById(spaceId)).thenReturn(Optional.of(space));

        // Act
        Optional<Space> result = service.findSpaceById(spaceId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(spaceId, result.get().getId());
    }
}
