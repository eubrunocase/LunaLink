package com.LunaLink.application.application.service.report;

import com.LunaLink.application.domain.enums.ReservationStatus;
import com.LunaLink.application.domain.enums.SpaceType;

import java.util.List;

public final class ReportFilters {

    public static final List<ReservationStatus> VALID_STATUSES = List.of(ReservationStatus.APPROVED);
    public static final List<SpaceType> BILLABLE_SPACE_TYPES = List.of(SpaceType.SALAO_FESTAS, SpaceType.CHURRASQUEIRA);

    private ReportFilters() {
    }
}
