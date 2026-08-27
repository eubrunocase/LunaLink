package com.LunaLink.application.infrastructure.config;

import com.LunaLink.application.domain.enums.SpaceType;

import java.util.*;

public final class SpaceEquipmentCatalog {

    private static final Map<SpaceType, List<String>> EQUIPMENT_MAP = Map.of(
            SpaceType.SALAO_FESTAS, List.of(
                    "Mesas",
                    "Cadeiras",
                    "Freezer 1",
                    "Freezer 2",
                    "Fogão",
                    "Televisão"
            ),
            SpaceType.CHURRASQUEIRA, List.of(
                    "Grelhas",
                    "Aparatos de churrasco",
                    "Cadeiras",
                    "Tábuas",
                    "Freezer"
            )
    );

    private SpaceEquipmentCatalog() {
    }

    public static List<String> getEquipmentForSpace(SpaceType spaceType) {
        return EQUIPMENT_MAP.getOrDefault(spaceType, Collections.emptyList());
    }

    public static boolean requiresInspection(SpaceType spaceType) {
        return EQUIPMENT_MAP.containsKey(spaceType);
    }
}
