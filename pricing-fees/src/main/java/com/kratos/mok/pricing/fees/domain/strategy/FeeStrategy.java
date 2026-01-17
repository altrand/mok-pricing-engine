package com.kratos.mok.pricing.fees.domain.strategy;

import com.kratos.mok.pricing.fees.domain.enums.FeeStrategyType;
import com.kratos.mok.pricing.shared.domain.vo.Money;

import java.math.BigDecimal;
import java.util.Objects;

public final class FeeStrategy {

    private final FeeStrategyType type;

    private final Money fixedFee;          // uniquement si FIXED
    private final BigDecimal percentage;   // uniquement si PERCENTAGE

    private FeeStrategy(FeeStrategyType type, Money fixedFee, BigDecimal percentage) {
        this.type = Objects.requireNonNull(type);
        this.fixedFee = fixedFee;
        this.percentage = percentage;
    }

    public static FeeStrategy fixed(Money fee) {
        Objects.requireNonNull(fee);
        if (fee.isNegative()) throw new IllegalArgumentException("Fixed fee cannot be negative");
        return new FeeStrategy(FeeStrategyType.FIXED, fee, null);
    }

    public static FeeStrategy percentage(BigDecimal percentage) {
        Objects.requireNonNull(percentage);
        if (percentage.signum() < 0) throw new IllegalArgumentException("Percentage cannot be negative");
        return new FeeStrategy(FeeStrategyType.PERCENTAGE, null, percentage);
    }

    public static FeeStrategy tiered() {
        return new FeeStrategy(FeeStrategyType.TIERED, null, null);
    }

    public FeeStrategyType type() { return type; }

    public Money fixedFee() {
        if (type != FeeStrategyType.FIXED) throw new IllegalStateException("Not FIXED");
        return fixedFee;
    }

    public BigDecimal percentage() {
        if (type != FeeStrategyType.PERCENTAGE) throw new IllegalStateException("Not PERCENTAGE");
        return percentage;
    }
}
