package app.tshepo.service.criteria;

import app.tshepo.domain.enumeration.CredentialStatus;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link app.tshepo.domain.Credential} entity. This class is used
 * in {@link app.tshepo.web.rest.CredentialResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /credentials?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CredentialCriteria implements Serializable, Criteria {

    /**
     * Class for filtering CredentialStatus
     */
    public static class CredentialStatusFilter extends Filter<CredentialStatus> {

        public CredentialStatusFilter() {}

        public CredentialStatusFilter(CredentialStatusFilter filter) {
            super(filter);
        }

        @Override
        public CredentialStatusFilter copy() {
            return new CredentialStatusFilter(this);
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter holderLogin;

    private StringFilter title;

    private StringFilter purpose;

    private CredentialStatusFilter status;

    private InstantFilter issuedAt;

    private InstantFilter expiresAt;

    private StringFilter issuerDid;

    private StringFilter claimsSummary;

    private StringFilter vcRef;

    private LongFilter claimsId;

    private Boolean distinct;

    public CredentialCriteria() {}

    public CredentialCriteria(CredentialCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.holderLogin = other.optionalHolderLogin().map(StringFilter::copy).orElse(null);
        this.title = other.optionalTitle().map(StringFilter::copy).orElse(null);
        this.purpose = other.optionalPurpose().map(StringFilter::copy).orElse(null);
        this.status = other.optionalStatus().map(CredentialStatusFilter::copy).orElse(null);
        this.issuedAt = other.optionalIssuedAt().map(InstantFilter::copy).orElse(null);
        this.expiresAt = other.optionalExpiresAt().map(InstantFilter::copy).orElse(null);
        this.issuerDid = other.optionalIssuerDid().map(StringFilter::copy).orElse(null);
        this.claimsSummary = other.optionalClaimsSummary().map(StringFilter::copy).orElse(null);
        this.vcRef = other.optionalVcRef().map(StringFilter::copy).orElse(null);
        this.claimsId = other.optionalClaimsId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public CredentialCriteria copy() {
        return new CredentialCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public Optional<LongFilter> optionalId() {
        return Optional.ofNullable(id);
    }

    public LongFilter id() {
        if (id == null) {
            setId(new LongFilter());
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getHolderLogin() {
        return holderLogin;
    }

    public Optional<StringFilter> optionalHolderLogin() {
        return Optional.ofNullable(holderLogin);
    }

    public StringFilter holderLogin() {
        if (holderLogin == null) {
            setHolderLogin(new StringFilter());
        }
        return holderLogin;
    }

    public void setHolderLogin(StringFilter holderLogin) {
        this.holderLogin = holderLogin;
    }

    public StringFilter getTitle() {
        return title;
    }

    public Optional<StringFilter> optionalTitle() {
        return Optional.ofNullable(title);
    }

    public StringFilter title() {
        if (title == null) {
            setTitle(new StringFilter());
        }
        return title;
    }

    public void setTitle(StringFilter title) {
        this.title = title;
    }

    public StringFilter getPurpose() {
        return purpose;
    }

    public Optional<StringFilter> optionalPurpose() {
        return Optional.ofNullable(purpose);
    }

    public StringFilter purpose() {
        if (purpose == null) {
            setPurpose(new StringFilter());
        }
        return purpose;
    }

    public void setPurpose(StringFilter purpose) {
        this.purpose = purpose;
    }

    public CredentialStatusFilter getStatus() {
        return status;
    }

    public Optional<CredentialStatusFilter> optionalStatus() {
        return Optional.ofNullable(status);
    }

    public CredentialStatusFilter status() {
        if (status == null) {
            setStatus(new CredentialStatusFilter());
        }
        return status;
    }

    public void setStatus(CredentialStatusFilter status) {
        this.status = status;
    }

    public InstantFilter getIssuedAt() {
        return issuedAt;
    }

    public Optional<InstantFilter> optionalIssuedAt() {
        return Optional.ofNullable(issuedAt);
    }

    public InstantFilter issuedAt() {
        if (issuedAt == null) {
            setIssuedAt(new InstantFilter());
        }
        return issuedAt;
    }

    public void setIssuedAt(InstantFilter issuedAt) {
        this.issuedAt = issuedAt;
    }

    public InstantFilter getExpiresAt() {
        return expiresAt;
    }

    public Optional<InstantFilter> optionalExpiresAt() {
        return Optional.ofNullable(expiresAt);
    }

    public InstantFilter expiresAt() {
        if (expiresAt == null) {
            setExpiresAt(new InstantFilter());
        }
        return expiresAt;
    }

    public void setExpiresAt(InstantFilter expiresAt) {
        this.expiresAt = expiresAt;
    }

    public StringFilter getIssuerDid() {
        return issuerDid;
    }

    public Optional<StringFilter> optionalIssuerDid() {
        return Optional.ofNullable(issuerDid);
    }

    public StringFilter issuerDid() {
        if (issuerDid == null) {
            setIssuerDid(new StringFilter());
        }
        return issuerDid;
    }

    public void setIssuerDid(StringFilter issuerDid) {
        this.issuerDid = issuerDid;
    }

    public StringFilter getClaimsSummary() {
        return claimsSummary;
    }

    public Optional<StringFilter> optionalClaimsSummary() {
        return Optional.ofNullable(claimsSummary);
    }

    public StringFilter claimsSummary() {
        if (claimsSummary == null) {
            setClaimsSummary(new StringFilter());
        }
        return claimsSummary;
    }

    public void setClaimsSummary(StringFilter claimsSummary) {
        this.claimsSummary = claimsSummary;
    }

    public StringFilter getVcRef() {
        return vcRef;
    }

    public Optional<StringFilter> optionalVcRef() {
        return Optional.ofNullable(vcRef);
    }

    public StringFilter vcRef() {
        if (vcRef == null) {
            setVcRef(new StringFilter());
        }
        return vcRef;
    }

    public void setVcRef(StringFilter vcRef) {
        this.vcRef = vcRef;
    }

    public LongFilter getClaimsId() {
        return claimsId;
    }

    public Optional<LongFilter> optionalClaimsId() {
        return Optional.ofNullable(claimsId);
    }

    public LongFilter claimsId() {
        if (claimsId == null) {
            setClaimsId(new LongFilter());
        }
        return claimsId;
    }

    public void setClaimsId(LongFilter claimsId) {
        this.claimsId = claimsId;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public Optional<Boolean> optionalDistinct() {
        return Optional.ofNullable(distinct);
    }

    public Boolean distinct() {
        if (distinct == null) {
            setDistinct(true);
        }
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CredentialCriteria that = (CredentialCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(holderLogin, that.holderLogin) &&
            Objects.equals(title, that.title) &&
            Objects.equals(purpose, that.purpose) &&
            Objects.equals(status, that.status) &&
            Objects.equals(issuedAt, that.issuedAt) &&
            Objects.equals(expiresAt, that.expiresAt) &&
            Objects.equals(issuerDid, that.issuerDid) &&
            Objects.equals(claimsSummary, that.claimsSummary) &&
            Objects.equals(vcRef, that.vcRef) &&
            Objects.equals(claimsId, that.claimsId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            holderLogin,
            title,
            purpose,
            status,
            issuedAt,
            expiresAt,
            issuerDid,
            claimsSummary,
            vcRef,
            claimsId,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CredentialCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalHolderLogin().map(f -> "holderLogin=" + f + ", ").orElse("") +
            optionalTitle().map(f -> "title=" + f + ", ").orElse("") +
            optionalPurpose().map(f -> "purpose=" + f + ", ").orElse("") +
            optionalStatus().map(f -> "status=" + f + ", ").orElse("") +
            optionalIssuedAt().map(f -> "issuedAt=" + f + ", ").orElse("") +
            optionalExpiresAt().map(f -> "expiresAt=" + f + ", ").orElse("") +
            optionalIssuerDid().map(f -> "issuerDid=" + f + ", ").orElse("") +
            optionalClaimsSummary().map(f -> "claimsSummary=" + f + ", ").orElse("") +
            optionalVcRef().map(f -> "vcRef=" + f + ", ").orElse("") +
            optionalClaimsId().map(f -> "claimsId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
