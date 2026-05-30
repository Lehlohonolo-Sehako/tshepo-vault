package app.tshepo.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link app.tshepo.domain.VerifierApiKey} entity.
 */
@Schema(
    description = "API key issued to a verifier for the metered POST /v1/verify endpoint.\nkeyHash is a bcrypt hash of the raw key. The raw key is returned once\non creation and never stored in plain text."
)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class VerifierApiKeyDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 50)
    private String ownerLogin;

    @NotNull
    @Size(max = 80)
    private String label;

    @NotNull
    @Size(max = 100)
    private String keyHash;

    @NotNull
    private Boolean active;

    @Min(value = 0L)
    private Long callCount;

    @NotNull
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOwnerLogin() {
        return ownerLogin;
    }

    public void setOwnerLogin(String ownerLogin) {
        this.ownerLogin = ownerLogin;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getKeyHash() {
        return keyHash;
    }

    public void setKeyHash(String keyHash) {
        this.keyHash = keyHash;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Long getCallCount() {
        return callCount;
    }

    public void setCallCount(Long callCount) {
        this.callCount = callCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VerifierApiKeyDTO)) {
            return false;
        }

        VerifierApiKeyDTO verifierApiKeyDTO = (VerifierApiKeyDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, verifierApiKeyDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "VerifierApiKeyDTO{" +
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
