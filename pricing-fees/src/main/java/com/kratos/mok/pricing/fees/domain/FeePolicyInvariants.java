package com.kratos.mok.pricing.fees.domain;

import com.kratos.mok.pricing.fees.domain.enums.FeeStrategyType;

final class FeePolicyInvariants {

    static void assertConsistent(FeePolicy p) {
        if (p.strategy().type() == FeeStrategyType.TIERED) {
            if (p.rules().tiers() == null || p.rules().tiers().isEmpty()) {
                throw new IllegalStateException("TIERED strategy requires rules.tiers");
            }
        } else {
            // Tu peux choisir : autoriser tiers non-null même si pas tiered, ou interdire.
            // Je conseille d'interdire pour éviter les configs ambiguës.
            if (p.rules().tiers() != null) {
                throw new IllegalStateException("tiers must be null unless strategy is TIERED");
            }
        }
    }

    private FeePolicyInvariants() {}
}

