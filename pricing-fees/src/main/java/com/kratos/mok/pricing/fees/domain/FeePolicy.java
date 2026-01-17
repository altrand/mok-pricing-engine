package com.kratos.mok.pricing.fees.domain;

import com.kratos.mok.pricing.fees.domain.enums.*;
import com.kratos.mok.pricing.fees.domain.vo.*;
import com.kratos.mok.pricing.shared.domain.vo.AuditInfo;
import com.kratos.mok.pricing.shared.domain.vo.Money;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

public class FeePolicy {

    private final FeePolicyId id;

    private final TransactionType transactionType;
    private final FeeTarget target;

    private FeeStrategy strategy;      // FIXED | PERCENTAGE | TIERED
    private FeeRules rules;            // activationThreshold, minFee, maxFee, tiers
    private KycRequirement kycRequirement;

    private ValidityWindow validity;   // start/end optionnel
    private PolicyPriority priority;   // int (tie-breaker explicite)

    private FeePolicyStatus status;    // DRAFT, PENDING_APPROVAL, ACTIVE, ...
    private AuditInfo created;
    private AuditInfo lastModified;

    private FeePolicy(
            FeePolicyId id,
            TransactionType transactionType,
            FeeTarget target,
            FeeStrategy strategy,
            FeeRules rules,
            KycRequirement kycRequirement,
            ValidityWindow validity,
            PolicyPriority priority,
            FeePolicyStatus status,
            AuditInfo created,
            AuditInfo lastModified
    ) {
        this.id = Objects.requireNonNull(id);
        this.transactionType = Objects.requireNonNull(transactionType);
        this.target = Objects.requireNonNull(target);

        this.strategy = Objects.requireNonNull(strategy);
        this.rules = Objects.requireNonNull(rules);
        this.kycRequirement = Objects.requireNonNull(kycRequirement);

        this.validity = Objects.requireNonNull(validity);
        this.priority = Objects.requireNonNull(priority);

        this.status = Objects.requireNonNull(status);
        this.created = Objects.requireNonNull(created);
        this.lastModified = lastModified;

        // Invariants de cohérence
        FeePolicyInvariants.assertConsistent(this);
    }

    // ---------- Factories ----------

    public static FeePolicy draft(
            FeePolicyId id,
            TransactionType transactionType,
            FeeTarget target,
            FeeStrategy strategy,
            FeeRules rules,
            KycRequirement kycRequirement,
            ValidityWindow validity,
            PolicyPriority priority,
            AuditInfo created
    ) {
        return new FeePolicy(
                id,
                transactionType,
                target,
                strategy,
                rules,
                kycRequirement,
                validity == null ? ValidityWindow.permanent() : validity,
                priority == null ? PolicyPriority.defaultFor(target.scope()) : priority,
                FeePolicyStatus.DRAFT,
                created,
                null
        );
    }

    // Reconstitution (event sourcing / persistence)
    public static FeePolicy reconstitute(
            FeePolicyId id,
            TransactionType transactionType,
            FeeTarget target,
            FeeStrategy strategy,
            FeeRules rules,
            KycRequirement kycRequirement,
            ValidityWindow validity,
            PolicyPriority priority,
            FeePolicyStatus status,
            AuditInfo created,
            AuditInfo lastModified
    ) {
        return new FeePolicy(
                id, transactionType, target, strategy, rules, kycRequirement,
                validity, priority, status, created, lastModified
        );
    }

    // ---------- Lifecycle ----------

    public void submitForApproval(String authorId, String reason, Clock clock) {
        requireMutable();
        if (status != FeePolicyStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT can be submitted. Current=" + status);
        }
        this.status = FeePolicyStatus.PENDING_APPROVAL;
        touch(authorId, reason, clock);
    }

    public void approve(String superAdminId, String reason, Clock clock) {
        if (status != FeePolicyStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Only PENDING_APPROVAL can be approved. Current=" + status);
        }
        this.status = FeePolicyStatus.ACTIVE;
        touch(superAdminId, reason, clock);
    }

    public void reject(String superAdminId, String reason, Clock clock) {
        if (status != FeePolicyStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Only PENDING_APPROVAL can be rejected. Current=" + status);
        }
        this.status = FeePolicyStatus.REJECTED;
        touch(superAdminId, reason, clock);
    }

    public void suspend(String authorId, String reason, Clock clock) {
        if (status != FeePolicyStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE can be suspended. Current=" + status);
        }
        this.status = FeePolicyStatus.SUSPENDED;
        touch(authorId, reason, clock);
    }

    public void archive(String authorId, String reason, Clock clock) {
        if (status == FeePolicyStatus.ARCHIVED) return;
        this.status = FeePolicyStatus.ARCHIVED;
        touch(authorId, reason, clock);
    }

    // ---------- Business: fee computation ----------

    public Money computeFee(TransactionContext ctx) {
        ensureActive();
        ensureValidAt(ctx.transactionDate());

        if (kycRequirement == KycRequirement.REQUIRED && !ctx.isKycValidated()) {
            throw new IllegalArgumentException("KYC required");
        }

        // Seuil d’activation (gratuit jusqu’à X)
        if (ctx.amount().compareTo(rules.activationThreshold()) <= 0) {
            return Money.ZERO;
        }

        Money base = switch (strategy.type()) {
            case FIXED -> strategy.fixedFee();
            case PERCENTAGE -> ctx.amount().multiply(strategy.percentage());
            case TIERED -> rules.findTierFor(ctx.amount()).compute(ctx.amount());
        };

        return rules.applyMinMax(base);
    }

    // ---------- Mutations métier (si tu veux les autoriser) ----------

    public void changeRules(String authorId, FeeRules newRules, String reason, Clock clock) {
        requireMutable();
        this.rules = Objects.requireNonNull(newRules);
        FeePolicyInvariants.assertConsistent(this);
        touch(authorId, reason, clock);
    }

    public void changeStrategy(String authorId, FeeStrategy newStrategy, String reason, Clock clock) {
        requireMutable();
        this.strategy = Objects.requireNonNull(newStrategy);
        FeePolicyInvariants.assertConsistent(this);
        touch(authorId, reason, clock);
    }

    // ---------- Helpers ----------

    private void ensureActive() {
        if (status != FeePolicyStatus.ACTIVE) {
            throw new IllegalStateException("Policy not ACTIVE. status=" + status);
        }
    }

    private void ensureValidAt(LocalDateTime dateTime) {
        if (!validity.isValidAt(dateTime)) {
            throw new IllegalArgumentException("Policy not valid at " + dateTime);
        }
    }

    private void requireMutable() {
        if (status == FeePolicyStatus.ARCHIVED) {
            throw new IllegalStateException("ARCHIVED policy is immutable");
        }
    }

    private void touch(String authorId, String reason, Clock clock) {
        this.lastModified = new AuditInfo(authorId, LocalDateTime.now(clock), reason);
    }

    // ---------- Getters (CQRS friendly) ----------

    public FeePolicyId id() { return id; }
    public TransactionType transactionType() { return transactionType; }
    public FeeTarget target() { return target; }
    public FeeStrategy strategy() { return strategy; }
    public FeeRules rules() { return rules; }
    public KycRequirement kycRequirement() { return kycRequirement; }
    public ValidityWindow validity() { return validity; }
    public PolicyPriority priority() { return priority; }
    public FeePolicyStatus status() { return status; }
    public AuditInfo created() { return created; }
    public AuditInfo lastModified() { return lastModified; }
}
