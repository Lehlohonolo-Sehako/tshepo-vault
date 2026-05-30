package app.tshepo.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * API key issued to a verifier for the metered POST /v1/verify endpoint.
 * keyHash is a bcrypt hash of the raw key. The raw key is returned once
 * on creation and never stored in plain text.
 */
@Entity
@Table(name = "verifier_api_key")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class VerifierApiKey implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 50)
    @Column(name = "owner_login", length = 50, nullable = false)
    private String ownerLogin;

    @NotNull
    @Size(max = 80)
    @Column(name = "label", length = 80, nullable = false)
    private String label;

    @NotNull
    @Size(max = 100)
    @Column(name = "key_hash", length = 100, nullable = false, unique = true)
    private String keyHash;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;

    @Min(value = 0L)
    @Column(name = "call_count")
    private Long callCount;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "key_uuid", unique = true)
    private UUID keyUuid;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public VerifierApiKey id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOwnerLogin() {
        return this.ownerLogin;
    }

    public VerifierApiKey ownerLogin(String ownerLogin) {
        this.setOwnerLogin(ownerLogin);
        return this;
    }

    public void setOwnerLogin(String ownerLogin) {
        this.ownerLogin = ownerLogin;
    }

    public String getLabel() {
        return this.label;
    }

    public VerifierApiKey label(String label) {
        this.setLabel(label);
        return this;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getKeyHash() {
        return this.keyHash;
    }

    public VerifierApiKey keyHash(String keyHash) {
        this.setKeyHash(keyHash);
        return this;
    }

    public void setKeyHash(String keyHash) {
        this.keyHash = keyHash;
    }

    public Boolean getActive() {
        return this.active;
    }

    public VerifierApiKey active(Boolean active) {
        this.setActive(active);
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Long getCallCount() {
        return this.callCount;
    }

    public VerifierApiKey callCount(Long callCount) {
        this.setCallCount(callCount);
        return this;
    }

    public void setCallCount(Long callCount) {
        this.callCount = callCount;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public VerifierApiKey createdAt(Instant createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public UUID getKeyUuid() {
        return this.keyUuid;
    }

    public VerifierApiKey keyUuid(UUID keyUuid) {
        this.keyUuid = keyUuid;
        return this;
    }

    public void setKeyUuid(UUID keyUuid) {
        this.keyUuid = keyUuid;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VerifierApiKey)) {
            return false;
        }
        return getId() != null && getId().equals(((VerifierApiKey) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "VerifierApiKey{" +
            "id=" + getId() +
            ", ownerLogin='" + getOwnerLogin() + "'" +
            ", label='" + getLabel() + "'" +
            ", keyHash='" + getKeyHash() + "'" +
            ", active='" + getActive() + "'" +
            ", callCount=" + getCallCount() +
            ", createdAt='" + getCreatedAt() + "'" +
            "}";
    }
}
