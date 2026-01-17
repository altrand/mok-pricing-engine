package com.kratos.mok.pricing.fees.domain.strategy;

import com.kratos.mok.pricing.shared.domain.vo.Money;
import java.math.BigDecimal;
import java.util.Objects;

public record FeeTier(
        Money minAmount,
        Money maxAmount,
        BigDecimal ratePercentage, // nullable si fixedFee != null
        Money fixedFee             // nullable si ratePercentage != null
) {

    public FeeTier {
        Objects.requireNonNull(minAmount);
        Objects.requireNonNull(maxAmount);

        if (minAmount.isNegative() || maxAmount.isNegative()) {
            throw new IllegalArgumentException("Tier bounds cannot be negative");
        }
        if (minAmount.compareTo(maxAmount) > 0) {
            throw new IllegalArgumentException("minAmount cannot be > maxAmount");
        }

        boolean hasRate = ratePercentage != null;
        boolean hasFixed = fixedFee != null;

        if (hasRate == hasFixed) {
            throw new IllegalArgumentException("Tier must define exactly one of (ratePercentage, fixedFee)");
        }

        if (hasRate && ratePercentage.signum() < 0) {
            throw new IllegalArgumentException("ratePercentage cannot be negative");
        }
        if (hasFixed && fixedFee.isNegative()) {
            throw new IllegalArgumentException("fixedFee cannot be negative");
        }
    }

    public boolean covers(Money amount) {
        return amount.compareTo(minAmount) >= 0 && amount.compareTo(maxAmount) <= 0;
    }

    public Money compute(Money amount) {
        if (fixedFee != null) return fixedFee;
        return amount.multiply(ratePercentage);
    }
}
