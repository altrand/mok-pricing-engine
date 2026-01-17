package com.kratos.mok.pricing.fees.application.command.createFeePolicy;

import com.kratos.mok.pricing.fees.domain.FeeLimits;
import com.kratos.mok.pricing.fees.domain.FeeTarget;
import com.kratos.mok.pricing.fees.domain.vo.ValidityWindow;
import com.kratos.mok.pricing.fees.domain.enums.TransactionType;
import com.kratos.mok.pricing.shared.domain.vo.Money;

public record CreateFeePolicyCommand(
        TransactionType type,
        FeeTarget target,
        FeeStrategy strategy,
        FeeLimits limits,
        Money activationThreshold,
        ValidityWindow validity,
        boolean kycRequired,
        String authorId
) {}
