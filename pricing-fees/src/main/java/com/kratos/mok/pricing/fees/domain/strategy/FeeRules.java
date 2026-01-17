package com.kratos.mok.pricing.fees.domain.strategy;

import com.kratos.mok.pricing.shared.domain.vo.Money;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public record FeeRules(
        Money activationThreshold,
        Money minFee,
        Money maxFee,
        List<FeeTier> tiers
) {

    public FeeRules {
        activationThreshold = activationThreshold == null ? Money.ZERO : activationThreshold;

        if (activationThreshold.isNegative()) {
            throw new IllegalArgumentException("activationThreshold cannot be negative");
        }

        if (minFee != null && minFee.isNegative()) throw new IllegalArgumentException("minFee cannot be negative");
        if (maxFee != null && maxFee.isNegative()) throw new IllegalArgumentException("maxFee cannot be negative");
        if (minFee != null && maxFee != null && minFee.compareTo(maxFee) > 0) {
            throw new IllegalArgumentException("minFee cannot be > maxFee");
        }

        if (tiers != null) {
            if (tiers.isEmpty()) throw new IllegalArgumentException("tiers cannot be empty if provided");
            validateTiers(tiers);
        }
    }

    public Money applyMinMax(Money computed) {
        Money result = computed;

        // minFee : optionnel, et tu peux décider s’il s’applique sur 0 ou non.
        if (minFee != null && !result.isZero() && result.compareTo(minFee) < 0) {
            result = minFee;
        }
        if (maxFee != null && result.compareTo(maxFee) > 0) {
            result = maxFee;
        }
        return result;
    }

    public FeeTier findTierFor(Money amount) {
        if (tiers == null) {
            throw new IllegalStateException("No tiers configured for TIERED policy");
        }
        return tiers.stream()
                .filter(t -> t.covers(amount))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No tier matches amount=" + amount));
    }

    private static void validateTiers(List<FeeTier> tiers) {
        List<FeeTier> sorted = tiers.stream()
                .sorted(Comparator.comparing(FeeTier::minAmount))
                .toList();

        FeeTier prev = null;
        for (FeeTier cur : sorted) {
            if (prev != null && cur.minAmount().compareTo(prev.maxAmount()) < 0) {
                throw new IllegalArgumentException("Tier overlap detected between " + prev + " and " + cur);
            }
            prev = cur;
        }
    }
}
