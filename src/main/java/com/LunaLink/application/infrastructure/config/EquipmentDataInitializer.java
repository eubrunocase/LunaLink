package com.LunaLink.application.infrastructure.config;

import com.LunaLink.application.domain.model.equipment.Equipment;
import com.LunaLink.application.infrastructure.repository.equipment.EquipmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class EquipmentDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(EquipmentDataInitializer.class);

    private static final String COMMUNITY_TV_NAME = "Televisão Comunitária";

    private final EquipmentRepository equipmentRepository;

    public EquipmentDataInitializer(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Seed idempotente: garante a existência da Televisão Comunitária (id = 1 fixo).
        // Necessário para bancos já criados, onde o init.sql do Docker não é reexecutado.
        if (equipmentRepository.findByName(COMMUNITY_TV_NAME).isPresent()) {
            return;
        }

        Equipment tv = new Equipment(COMMUNITY_TV_NAME);
        equipmentRepository.save(tv);
        log.info("Equipamento padrão '{}' criado com id={}", COMMUNITY_TV_NAME, tv.getId());
    }
}
