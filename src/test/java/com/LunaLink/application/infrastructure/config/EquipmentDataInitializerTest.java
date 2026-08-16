package com.LunaLink.application.infrastructure.config;

import com.LunaLink.application.domain.model.equipment.Equipment;
import com.LunaLink.application.infrastructure.repository.equipment.EquipmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipmentDataInitializerTest {

    @Mock
    private EquipmentRepository equipmentRepository;

    @InjectMocks
    private EquipmentDataInitializer initializer;

    @Mock
    private ApplicationArguments args;

    @Test
    @DisplayName("Não deve duplicar o equipamento quando ele já existe")
    void run_ShouldNotCreate_WhenEquipmentExists() {
        // Arrange
        Equipment existing = new Equipment("Televisão Comunitária");
        existing.setId(1L);
        when(equipmentRepository.findByName("Televisão Comunitária")).thenReturn(Optional.of(existing));

        // Act
        initializer.run(args);

        // Assert
        verify(equipmentRepository, never()).save(any(Equipment.class));
    }

    @Test
    @DisplayName("Deve criar o equipamento padrão quando ausente")
    void run_ShouldCreateEquipment_WhenMissing() {
        // Arrange
        when(equipmentRepository.findByName("Televisão Comunitária")).thenReturn(Optional.empty());
        Equipment created = new Equipment("Televisão Comunitária");
        created.setId(1L);
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(created);

        // Act
        initializer.run(args);

        // Assert
        verify(equipmentRepository, times(1)).save(any(Equipment.class));
        assertEquals("Televisão Comunitária", created.getName());
    }
}
