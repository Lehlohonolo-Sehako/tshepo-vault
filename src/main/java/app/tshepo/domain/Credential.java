package app.tshepo.domain;

import app.tshepo.domain.enumeration.CredentialStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A signed SD-JWT Verifiable Credential held by one Investec customer.
 * sdJwt stores the full compact serialisation; it is never sent back to
 * the client in list views — only on explicit single-credential fetch.
 */
@Entity
@Table(name = "credential")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Credential implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 50)
    @Column(name = "holder_login", length = 50, nullable = false)
    private String holderLogin;

    @NotNull
    @Size(max = 120)
    @Column(name = "title", length = 120, nullable = false)
    private String title;

    @Size(max = 200)
    @Column(name = "purpose", length = 200)
    private String purpose;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CredentialStatus status;

    @NotNull
    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @NotNull
    @Size(max = 200)
    @Column(name = "issuer_did", length = 200, nullable = false)
    private String issuerDid;

    @Lob
    @Column(name = "sd_jwt", nullable = false)
    private String sdJwt;

    /**
     * Comma-separated short claim labels for list-view display only.
     */
    @Size(max = 500)
    @Column(name = "claims_summary", length = 500)
    private String claimsSummary;

    @Size(max = 200)
    @Column(name = "vc_ref", length = 200)
    private String vcRef;

    @Column(name = "credential_uuid", unique = true)
    private UUID credentialUuid;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "credential")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "credential" }, allowSetters = true)
    private Set<IssuedClaim> claimses = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Credential id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHolderLogin() {
        return this.holderLogin;
    }

    public Credential holderLogin(String holderLogin) {
        this.setHolderLogin(holderLogin);
        return this;
    }

    public void setHolderLogin(String holderLogin) {
        this.holderLogin = holderLogin;
    }

    public String getTitle() {
        return this.title;
    }

    public Credential title(String title) {
        this.setTitle(title);
        return this;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPurpose() {
        return this.purpose;
    }

    public Credential purpose(String purpose) {
        this.setPurpose(purpose);
        return this;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public CredentialStatus getStatus() {
        return this.status;
    }

    public Credential status(CredentialStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(CredentialStatus status) {
        this.status = status;
    }

    public Instant getIssuedAt() {
        return this.issuedAt;
    }

    public Credential issuedAt(Instant issuedAt) {
        this.setIssuedAt(issuedAt);
        return this;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Instant getExpiresAt() {
        return this.expiresAt;
    }

    public Credential expiresAt(Instant expiresAt) {
        this.setExpiresAt(expiresAt);
        return this;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getIssuerDid() {
        return this.issuerDid;
    }

    public Credential issuerDid(String issuerDid) {
        this.setIssuerDid(issuerDid);
        return this;
    }

    public void setIssuerDid(String issuerDid) {
        this.issuerDid = issuerDid;
    }

    public String getSdJwt() {
        return this.sdJwt;
    }

    public Credential sdJwt(String sdJwt) {
        this.setSdJwt(sdJwt);
        return this;
    }

    public void setSdJwt(String sdJwt) {
        this.sdJwt = sdJwt;
    }

    public String getClaimsSummary() {
        return this.claimsSummary;
    }

    public Credential claimsSummary(String claimsSummary) {
        this.setClaimsSummary(claimsSummary);
        return this;
    }

    public void setClaimsSummary(String claimsSummary) {
        this.claimsSummary = claimsSummary;
    }

    public String getVcRef() {
        return this.vcRef;
    }

    public Credential vcRef(String vcRef) {
        this.setVcRef(vcRef);
        return this;
    }

    public void setVcRef(String vcRef) {
        this.vcRef = vcRef;
    }

    public UUID getCredentialUuid() {
        return this.credentialUuid;
    }

    public Credential credentialUuid(UUID credentialUuid) {
        this.setCredentialUuid(credentialUuid);
        return this;
    }

    public void setCredentialUuid(UUID credentialUuid) {
        this.credentialUuid = credentialUuid;
    }

    public Set<IssuedClaim> getClaimses() {
        return this.claimses;
    }

    public void setClaimses(Set<IssuedClaim> issuedClaims) {
        if (this.claimses != null) {
            this.claimses.forEach(i -> i.setCredential(null));
        }
        if (issuedClaims != null) {
            issuedClaims.forEach(i -> i.setCredential(this));
        }
        this.claimses = issuedClaims;
    }

    public Credential claimses(Set<IssuedClaim> issuedClaims) {
        this.setClaimses(issuedClaims);
        return this;
    }

    public Credential addClaims(IssuedClaim issuedClaim) {
        this.claimses.add(issuedClaim);
        issuedClaim.setCredential(this);
        return this;
    }

    public Credential removeClaims(IssuedClaim issuedClaim) {
        this.claimses.remove(issuedClaim);
        issuedClaim.setCredential(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Credential)) {
            return false;
        }
        return getId() != null && getId().equals(((Credential) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Credential{" +
            "id=" + getId() +
            ", holderLogin='" + getHolderLogin() + "'" +
            ", title='" + getTitle() + "'" +
            ", purpose='" + getPurpose() + "'" +
            ", status='" + getStatus() + "'" +
            ", issuedAt='" + getIssuedAt() + "'" +
            ", expiresAt='" + getExpiresAt() + "'" +
            ", issuerDid='" + getIssuerDid() + "'" +
            ", sdJwt='" + getSdJwt() + "'" +
            ", claimsSummary='" + getClaimsSummary() + "'" +
            ", vcRef='" + getVcRef() + "'" +
            "}";
    }
}
