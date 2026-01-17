package com.kratos.mok.pricing.fees.domain.strategy;

import com.kratos.mok.pricing.shared.domain.vo.Money;
import java.math.BigDecimal;
import java.util.Objects;

public sealed interface FeeTierCharge permits FeeTierCharge.Fixed, FeeTierCharge.Percentage {

    Money compute(Money amount);

    record Fixed(Money fee) implements FeeTierCharge {
        public Fixed {
            Objects.requireNonNull(fee, "fee");
            if (fee.isNegative()) throw new IllegalArgumentException("fixed fee negative");
        }
        @Override public Money compute(Money amount) { return fee; }
    }

    record Percentage(BigDecimal rate) implements FeeTierCharge {
        public Percentage {
            Objects.requireNonNull(rate, "rate");
            if (rate.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("rate negative");
        }
        @Override public Money compute(Money amount) { return amount.multiply(rate); }
    }
}
