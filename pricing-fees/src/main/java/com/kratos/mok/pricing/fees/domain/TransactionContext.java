package com.kratos.mok.pricing.fees.domain;

import com.kratos.mok.pricing.fees.domain.enums.TransactionType;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import java.time.LocalDateTime;
import java.util.Objects;

public record TransactionContext(
        TransactionType transactionType,
        Money amount,
        LocalDateTime occurredAt,
        String accountId,
        String accountType,
        boolean kycValidated
) {
    public TransactionContext {
        Objects.requireNonNull(transactionType);
        Objects.requireNonNull(amount);
        Objects.requireNonNull(occurredAt);
        if (accountId == null || accountId.isBlank()) throw new IllegalArgumentException("accountId blank");
        if (accountType == null || accountType.isBlank()) throw new IllegalArgumentException("accountType blank");
    }
}

