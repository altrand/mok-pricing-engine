package com.kratos.mok.pricing.fees.domain.service;

import com.kratos.mok.pricing.fees.domain.FeePolicy;
import com.kratos.mok.pricing.fees.domain.TransactionContext;

import java.util.Comparator;
import java.util.List;

public final class FeePolicySelector {

    public FeePolicy selectBest(List<FeePolicy> candidates, TransactionContext ctx) {
        return candidates.stream()
                .filter(p -> p.matches(ctx))
                .max(Comparator
                        .comparingInt((FeePolicy p) -> p.priority().value())
                        .thenComparingInt(FeePolicy::specificity)
                        // optionnel : tie-breaker stable
                        .thenComparing(p -> p.lastModified() != null ? p.lastModified().timestamp() : p.created().timestamp())
                )
                .orElseThrow(() -> new IllegalStateException("No matching ACTIVE policy for ctx"));
    }
}
