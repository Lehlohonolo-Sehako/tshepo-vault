package app.tshepo.service.dto;

import app.tshepo.domain.enumeration.ClaimOperator;
import app.tshepo.domain.enumeration.ClaimType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A DTO for the {@link app.tshepo.domain.IssuedClaim} entity.
 */
@Schema(
    description = "One predicate fact certified inside a Credential.\nPRIVACY: `met` is the only boolean stored. The raw evidence\n(e.g. \"R42,300\") is computed in memory, shown to the holder\nduring issuance, and immediately discarded — it never persists here."
)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class IssuedClaimDTO implements Serializable {

    private Long id;

    @NotNull
    private ClaimType claimType;

    @NotNull
    private ClaimOperator operator;

    @NotNull
    @DecimalMin(value = "0")
    @Schema(description = "Threshold value (Rand for monetary types, months for TENURE).", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal threshold;

    @Size(max = 3)
    private String currency;

    @Min(value = 1)
    @Max(value = 60)
    @Schema(description = "Look-back window used when computing this claim.")
    private Integer periodMonths;

    @NotNull
    @Schema(description = "True if the holder satisfied this threshold at issuance time.", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean met;

    @NotNull
    private CredentialDTO credential;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ClaimType getClaimType() {
        return claimType;
    }

    public void setClaimType(ClaimType claimType) {
        this.claimType = claimType;
    }

    public ClaimOperator getOperator() {
        return operator;
    }

    public void setOperator(ClaimOperator operator) {
        this.operator = operator;
    }

    public BigDecimal getThreshold() {
        return threshold;
    }

    public void setThreshold(BigDecimal threshold) {
        this.threshold = threshold;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getPeriodMonths() {
        return periodMonths;
    }

    public void setPeriodMonths(Integer periodMonths) {
        this.periodMonths = periodMonths;
    }

    public Boolean getMet() {
        return met;
    }

    public void setMet(Boolean met) {
        this.met = met;
    }

    public CredentialDTO getCredential() {
        return credential;
    }

    public void setCredential(CredentialDTO credential) {
        this.credential = credential;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IssuedClaimDTO)) {
            return false;
        }

        IssuedClaimDTO issuedClaimDTO = (IssuedClaimDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, issuedClaimDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "IssuedClaimDTO{" +
            "id=" + getId() +
            ", claimType='" + getClaimType() + "'" +
            ", operator='" + getOperator() + "'" +
            ", threshold=" + getThreshold() +
            ", currency='" + getCurrency() + "'" +
            ", periodMonths=" + getPeriodMonths() +
            ", met='" + getMet() + "'" +
            ", credential=" + getCredential() +
            "}";
    }
}
