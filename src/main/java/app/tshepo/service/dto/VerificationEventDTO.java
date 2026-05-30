package app.tshepo.service.dto;

import app.tshepo.domain.enumeration.VerificationResult;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link app.tshepo.domain.VerificationEvent} entity.
 */
@Schema(
    description = "Append-only audit log of every verification attempt.\napiKey is null when the check came from the public browser page.\ndisclosedClaims records which ClaimType values the holder revealed —\nstored as a JSON array string for portability."
)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class VerificationEventDTO implements Serializable {

    private Long id;

    @NotNull
    private Instant verifiedAt;

    @NotNull
    private VerificationResult result;

    @Schema(description = "JSON array of ClaimType strings that were disclosed.")
    @Lob
    private String disclosedClaims;

    @Size(max = 200)
    @Schema(description = "URN reference to the credential that was presented (for audit).")
    private String credentialRef;

    private VerifierApiKeyDTO apiKey;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public VerificationResult getResult() {
        return result;
    }

    public void setResult(VerificationResult result) {
        this.result = result;
    }

    public String getDisclosedClaims() {
        return disclosedClaims;
    }

    public void setDisclosedClaims(String disclosedClaims) {
        this.disclosedClaims = disclosedClaims;
    }

    public String getCredentialRef() {
        return credentialRef;
    }

    public void setCredentialRef(String credentialRef) {
        this.credentialRef = credentialRef;
    }

    public VerifierApiKeyDTO getApiKey() {
        return apiKey;
    }

    public void setApiKey(VerifierApiKeyDTO apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VerificationEventDTO)) {
            return false;
        }

        VerificationEventDTO verificationEventDTO = (VerificationEventDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, verificationEventDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "VerificationEventDTO{" +
            "id=" + getId() +
            ", verifiedAt='" + getVerifiedAt() + "'" +
            ", result='" + getResult() + "'" +
            ", disclosedClaims='" + getDisclosedClaims() + "'" +
            ", credentialRef='" + getCredentialRef() + "'" +
            ", apiKey=" + getApiKey() +
            "}";
    }
}
