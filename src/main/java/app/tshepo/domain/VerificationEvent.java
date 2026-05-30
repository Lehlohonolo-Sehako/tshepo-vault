package app.tshepo.domain;

import app.tshepo.domain.enumeration.VerificationResult;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Append-only audit log of every verification attempt.
 * apiKey is null when the check came from the public browser page.
 * disclosedClaims records which ClaimType values the holder revealed —
 * stored as a JSON array string for portability.
 */
@Entity
@Table(name = "verification_event")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class VerificationEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "verified_at", nullable = false)
    private Instant verifiedAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false)
    private VerificationResult result;

    /**
     * JSON array of ClaimType strings that were disclosed.
     */
    @Lob
    @Column(name = "disclosed_claims")
    private String disclosedClaims;

    /**
     * URN reference to the credential that was presented (for audit).
     */
    @Size(max = 200)
    @Column(name = "credential_ref", length = 200)
    private String credentialRef;

    @ManyToOne(fetch = FetchType.LAZY)
    private VerifierApiKey apiKey;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public VerificationEvent id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getVerifiedAt() {
        return this.verifiedAt;
    }

    public VerificationEvent verifiedAt(Instant verifiedAt) {
        this.setVerifiedAt(verifiedAt);
        return this;
    }

    public void setVerifiedAt(Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public VerificationResult getResult() {
        return this.result;
    }

    public VerificationEvent result(VerificationResult result) {
        this.setResult(result);
        return this;
    }

    public void setResult(VerificationResult result) {
        this.result = result;
    }

    public String getDisclosedClaims() {
        return this.disclosedClaims;
    }

    public VerificationEvent disclosedClaims(String disclosedClaims) {
        this.setDisclosedClaims(disclosedClaims);
        return this;
    }

    public void setDisclosedClaims(String disclosedClaims) {
        this.disclosedClaims = disclosedClaims;
    }

    public String getCredentialRef() {
        return this.credentialRef;
    }

    public VerificationEvent credentialRef(String credentialRef) {
        this.setCredentialRef(credentialRef);
        return this;
    }

    public void setCredentialRef(String credentialRef) {
        this.credentialRef = credentialRef;
    }

    public VerifierApiKey getApiKey() {
        return this.apiKey;
    }

    public void setApiKey(VerifierApiKey verifierApiKey) {
        this.apiKey = verifierApiKey;
    }

    public VerificationEvent apiKey(VerifierApiKey verifierApiKey) {
        this.setApiKey(verifierApiKey);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VerificationEvent)) {
            return false;
        }
        return getId() != null && getId().equals(((VerificationEvent) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "VerificationEvent{" +
            "id=" + getId() +
            ", verifiedAt='" + getVerifiedAt() + "'" +
            ", result='" + getResult() + "'" +
            ", disclosedClaims='" + getDisclosedClaims() + "'" +
            ", credentialRef='" + getCredentialRef() + "'" +
            "}";
    }
}
