package com.kratos.mok.pricing.fees.domain.vo;

import java.time.LocalDateTime;

public record ValidityWindow(LocalDateTime start, LocalDateTime end) {

    public static ValidityWindow permanent() {
        return new ValidityWindow(null, null);
    }

    public ValidityWindow {
        if (start != null && end != null && start.isAfter(end)) {
            throw new IllegalArgumentException("start cannot be after end");
        }
    }

    public boolean isValidAt(LocalDateTime dateTime) {
        if (start != null && dateTime.isBefore(start)) return false;
        if (end != null && dateTime.isAfter(end)) return false;
        return true;
    }
}

