package app.tshepo.domain;

import app.tshepo.domain.enumeration.BankConnectionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * OAuth token metadata for one holder's Investec connection.
 * Access/refresh tokens are stored encrypted in application config —
 * NEVER as plain-text fields here. This record holds only metadata.
 */
@Entity
@Table(name = "bank_connection")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class BankConnection implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 50)
    @Column(name = "holder_login", length = 50, nullable = false, unique = true)
    private String holderLogin;

    @NotNull
    @Column(name = "connected_at", nullable = false)
    private Instant connectedAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BankConnectionStatus status;

    /**
     * Masked account number shown in the UI, e.g. \"•••• 4821\".
     */
    @Size(max = 20)
    @Column(name = "masked_account", length = 20)
    private String maskedAccount;

    @Size(max = 80)
    @Column(name = "account_type", length = 80)
    private String accountType;

    @Lob
    @Column(name = "access_token")
    private String accessToken;

    @Lob
    @Column(name = "pre_computed_claims_json")
    private String preComputedClaimsJson;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public BankConnection id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHolderLogin() {
        return this.holderLogin;
    }

    public BankConnection holderLogin(String holderLogin) {
        this.setHolderLogin(holderLogin);
        return this;
    }

    public void setHolderLogin(String holderLogin) {
        this.holderLogin = holderLogin;
    }

    public Instant getConnectedAt() {
        return this.connectedAt;
    }

    public BankConnection connectedAt(Instant connectedAt) {
        this.setConnectedAt(connectedAt);
        return this;
    }

    public void setConnectedAt(Instant connectedAt) {
        this.connectedAt = connectedAt;
    }

    public BankConnectionStatus getStatus() {
        return this.status;
    }

    public BankConnection status(BankConnectionStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(BankConnectionStatus status) {
        this.status = status;
    }

    public String getMaskedAccount() {
        return this.maskedAccount;
    }

    public BankConnection maskedAccount(String maskedAccount) {
        this.setMaskedAccount(maskedAccount);
        return this;
    }

    public void setMaskedAccount(String maskedAccount) {
        this.maskedAccount = maskedAccount;
    }

    public String getAccountType() {
        return this.accountType;
    }

    public BankConnection accountType(String accountType) {
        this.setAccountType(accountType);
        return this;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public BankConnection accessToken(String accessToken) {
        this.setAccessToken(accessToken);
        return this;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getPreComputedClaimsJson() {
        return this.preComputedClaimsJson;
    }

    public BankConnection preComputedClaimsJson(String json) {
        this.preComputedClaimsJson = json;
        return this;
    }

    public void setPreComputedClaimsJson(String preComputedClaimsJson) {
        this.preComputedClaimsJson = preComputedClaimsJson;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BankConnection)) {
            return false;
        }
        return getId() != null && getId().equals(((BankConnection) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "BankConnection{" +
            "id=" + getId() +
            ", holderLogin='" + getHolderLogin() + "'" +
            ", connectedAt='" + getConnectedAt() + "'" +
            ", status='" + getStatus() + "'" +
            ", maskedAccount='" + getMaskedAccount() + "'" +
            ", accountType='" + getAccountType() + "'" +
            "}";
    }
}
