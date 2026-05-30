package app.tshepo.domain;

import app.tshepo.domain.enumeration.ClaimOperator;
import app.tshepo.domain.enumeration.ClaimType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * One predicate fact certified inside a Credential.
 * PRIVACY: `met` is the only boolean stored. The raw evidence
 * (e.g. \"R42,300\") is computed in memory, shown to the holder
 * during issuance, and immediately discarded — it never persists here.
 */
@Entity
@Table(name = "issued_claim")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class IssuedClaim implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "claim_type", nullable = false)
    private ClaimType claimType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "operator", nullable = false)
    private ClaimOperator operator;

    /**
     * Threshold value (Rand for monetary types, months for TENURE).
     */
    @NotNull
    @DecimalMin(value = "0")
    @Column(name = "threshold", precision = 21, scale = 2, nullable = false)
    private BigDecimal threshold;

    @Size(max = 3)
    @Column(name = "currency", length = 3)
    private String currency;

    /**
     * Look-back window used when computing this claim.
     */
    @Min(value = 1)
    @Max(value = 60)
    @Column(name = "period_months")
    private Integer periodMonths;

    /**
     * True if the holder satisfied this threshold at issuance time.
     */
    @NotNull
    @Column(name = "met", nullable = false)
    private Boolean met;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "claimses" }, allowSetters = true)
    private Credential credential;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public IssuedClaim id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ClaimType getClaimType() {
        return this.claimType;
    }

    public IssuedClaim claimType(ClaimType claimType) {
        this.setClaimType(claimType);
        return this;
    }

    public void setClaimType(ClaimType claimType) {
        this.claimType = claimType;
    }

    public ClaimOperator getOperator() {
        return this.operator;
    }

    public IssuedClaim operator(ClaimOperator operator) {
        this.setOperator(operator);
        return this;
    }

    public void setOperator(ClaimOperator operator) {
        this.operator = operator;
    }

    public BigDecimal getThreshold() {
        return this.threshold;
    }

    public IssuedClaim threshold(BigDecimal threshold) {
        this.setThreshold(threshold);
        return this;
    }

    public void setThreshold(BigDecimal threshold) {
        this.threshold = threshold;
    }

    public String getCurrency() {
        return this.currency;
    }

    public IssuedClaim currency(String currency) {
        this.setCurrency(currency);
        return this;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getPeriodMonths() {
        return this.periodMonths;
    }

    public IssuedClaim periodMonths(Integer periodMonths) {
        this.setPeriodMonths(periodMonths);
        return this;
    }

    public void setPeriodMonths(Integer periodMonths) {
        this.periodMonths = periodMonths;
    }

    public Boolean getMet() {
        return this.met;
    }

    public IssuedClaim met(Boolean met) {
        this.setMet(met);
        return this;
    }

    public void setMet(Boolean met) {
        this.met = met;
    }

    public Credential getCredential() {
        return this.credential;
    }

    public void setCredential(Credential credential) {
        this.credential = credential;
    }

    public IssuedClaim credential(Credential credential) {
        this.setCredential(credential);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IssuedClaim)) {
            return false;
        }
        return getId() != null && getId().equals(((IssuedClaim) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "IssuedClaim{" +
            "id=" + getId() +
            ", claimType='" + getClaimType() + "'" +
            ", operator='" + getOperator() + "'" +
            ", threshold=" + getThreshold() +
            ", currency='" + getCurrency() + "'" +
            ", periodMonths=" + getPeriodMonths() +
            ", met='" + getMet() + "'" +
            "}";
    }
}
