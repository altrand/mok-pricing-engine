package com.kratos.mok.pricing.fees.domain;

import com.kratos.mok.pricing.fees.domain.enums.TargetScope;

import java.util.Objects;

public record FeeTarget(TargetScope scope, String targetValue) {

    public FeeTarget {
        Objects.requireNonNull(scope);
        if (targetValue == null || targetValue.isBlank()) {
            throw new IllegalArgumentException("targetValue cannot be null/blank");
        }
        if (scope == TargetScope.GLOBAL && !"ALL".equals(targetValue)) {
            throw new IllegalArgumentException("GLOBAL targetValue must be ALL");
        }
    }

    public static FeeTarget global() {
        return new FeeTarget(TargetScope.GLOBAL, "ALL");
    }

    public static FeeTarget accountType(String accountType) {
        return new FeeTarget(TargetScope.ACCOUNT_TYPE, accountType);
    }

    public static FeeTarget accountId(String accountId) {
        return new FeeTarget(TargetScope.ACCOUNT_ID, accountId);
    }
}
