package com.LunaLink.application.application.ports.input;

import java.util.UUID;

public interface LiabilityTermServicePort {

    void signTerm(UUID reservationId, UUID residentId);
}
