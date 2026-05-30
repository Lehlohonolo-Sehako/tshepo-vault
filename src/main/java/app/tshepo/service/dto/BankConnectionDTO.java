package app.tshepo.service.dto;

import app.tshepo.domain.enumeration.BankConnectionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link app.tshepo.domain.BankConnection} entity.
 */
@Schema(
    description = "OAuth token metadata for one holder's Investec connection.\nAccess/refresh tokens are stored encrypted in application config —\nNEVER as plain-text fields here. This record holds only metadata."
)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class BankConnectionDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 50)
    private String holderLogin;

    @NotNull
    private Instant connectedAt;

    @NotNull
    private BankConnectionStatus status;

    @Size(max = 20)
    @Schema(description = "Masked account number shown in the UI, e.g. \"•••• 4821\".")
    private String maskedAccount;

    @Size(max = 80)
    private String accountType;

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

    public Instant getConnectedAt() {
        return connectedAt;
    }

    public void setConnectedAt(Instant connectedAt) {
        this.connectedAt = connectedAt;
    }

    public BankConnectionStatus getStatus() {
        return status;
    }

    public void setStatus(BankConnectionStatus status) {
        this.status = status;
    }

    public String getMaskedAccount() {
        return maskedAccount;
    }

    public void setMaskedAccount(String maskedAccount) {
        this.maskedAccount = maskedAccount;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BankConnectionDTO)) {
            return false;
        }

        BankConnectionDTO bankConnectionDTO = (BankConnectionDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, bankConnectionDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "BankConnectionDTO{" +
            "id=" + getId() +
            ", holderLogin='" + getHolderLogin() + "'" +
            ", connectedAt='" + getConnectedAt() + "'" +
            ", status='" + getStatus() + "'" +
            ", maskedAccount='" + getMaskedAccount() + "'" +
            ", accountType='" + getAccountType() + "'" +
            "}";
    }
}
