package app.tshepo.service.dto;

import app.tshepo.domain.enumeration.CredentialStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link app.tshepo.domain.Credential} entity.
 */
@Schema(
    description = "A signed SD-JWT Verifiable Credential held by one Investec customer.\nsdJwt stores the full compact serialisation; it is never sent back to\nthe client in list views — only on explicit single-credential fetch."
)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CredentialDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 50)
    private String holderLogin;

    @NotNull
    @Size(max = 120)
    private String title;

    @Size(max = 200)
    private String purpose;

    @NotNull
    private CredentialStatus status;

    @NotNull
    private Instant issuedAt;

    @NotNull
    private Instant expiresAt;

    @NotNull
    @Size(max = 200)
    private String issuerDid;

    @Lob
    private String sdJwt;

    @Size(max = 500)
    @Schema(description = "Comma-separated short claim labels for list-view display only.")
    private String claimsSummary;

    @Size(max = 200)
    private String vcRef;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHolderLogin() {
        return holderLogin;
    }

    public void setHolderLogin(String holderLogin) {
        this.holderLogin = holderLogin;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public CredentialStatus getStatus() {
        return status;
    }

    public void setStatus(CredentialStatus status) {
        this.status = status;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getIssuerDid() {
        return issuerDid;
    }

    public void setIssuerDid(String issuerDid) {
        this.issuerDid = issuerDid;
    }

    public String getSdJwt() {
        return sdJwt;
    }

    public void setSdJwt(String sdJwt) {
        this.sdJwt = sdJwt;
    }

    public String getClaimsSummary() {
        return claimsSummary;
    }

    public void setClaimsSummary(String claimsSummary) {
        this.claimsSummary = claimsSummary;
    }

    public String getVcRef() {
        return vcRef;
    }

    public void setVcRef(String vcRef) {
        this.vcRef = vcRef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CredentialDTO)) {
            return false;
        }

        CredentialDTO credentialDTO = (CredentialDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, credentialDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CredentialDTO{" +
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
