package app.tshepo.service.criteria;

import app.tshepo.domain.enumeration.VerificationResult;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link app.tshepo.domain.VerificationEvent} entity. This class is used
 * in {@link app.tshepo.web.rest.VerificationEventResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /verification-events?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class VerificationEventCriteria implements Serializable, Criteria {

    /**
     * Class for filtering VerificationResult
     */
    public static class VerificationResultFilter extends Filter<VerificationResult> {

        public VerificationResultFilter() {}

        public VerificationResultFilter(VerificationResultFilter filter) {
            super(filter);
        }

        @Override
        public VerificationResultFilter copy() {
            return new VerificationResultFilter(this);
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private InstantFilter verifiedAt;

    private VerificationResultFilter result;

    private StringFilter credentialRef;

    private LongFilter apiKeyId;

    private Boolean distinct;

    public VerificationEventCriteria() {}

    public VerificationEventCriteria(VerificationEventCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.verifiedAt = other.optionalVerifiedAt().map(InstantFilter::copy).orElse(null);
        this.result = other.optionalResult().map(VerificationResultFilter::copy).orElse(null);
        this.credentialRef = other.optionalCredentialRef().map(StringFilter::copy).orElse(null);
        this.apiKeyId = other.optionalApiKeyId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public VerificationEventCriteria copy() {
        return new VerificationEventCriteria(this);
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

    public InstantFilter getVerifiedAt() {
        return verifiedAt;
    }

    public Optional<InstantFilter> optionalVerifiedAt() {
        return Optional.ofNullable(verifiedAt);
    }

    public InstantFilter verifiedAt() {
        if (verifiedAt == null) {
            setVerifiedAt(new InstantFilter());
        }
        return verifiedAt;
    }

    public void setVerifiedAt(InstantFilter verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public VerificationResultFilter getResult() {
        return result;
    }

    public Optional<VerificationResultFilter> optionalResult() {
        return Optional.ofNullable(result);
    }

    public VerificationResultFilter result() {
        if (result == null) {
            setResult(new VerificationResultFilter());
        }
        return result;
    }

    public void setResult(VerificationResultFilter result) {
        this.result = result;
    }

    public StringFilter getCredentialRef() {
        return credentialRef;
    }

    public Optional<StringFilter> optionalCredentialRef() {
        return Optional.ofNullable(credentialRef);
    }

    public StringFilter credentialRef() {
        if (credentialRef == null) {
            setCredentialRef(new StringFilter());
        }
        return credentialRef;
    }

    public void setCredentialRef(StringFilter credentialRef) {
        this.credentialRef = credentialRef;
    }

    public LongFilter getApiKeyId() {
        return apiKeyId;
    }

    public Optional<LongFilter> optionalApiKeyId() {
        return Optional.ofNullable(apiKeyId);
    }

    public LongFilter apiKeyId() {
        if (apiKeyId == null) {
            setApiKeyId(new LongFilter());
        }
        return apiKeyId;
    }

    public void setApiKeyId(LongFilter apiKeyId) {
        this.apiKeyId = apiKeyId;
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
        final VerificationEventCriteria that = (VerificationEventCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(verifiedAt, that.verifiedAt) &&
            Objects.equals(result, that.result) &&
            Objects.equals(credentialRef, that.credentialRef) &&
            Objects.equals(apiKeyId, that.apiKeyId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, verifiedAt, result, credentialRef, apiKeyId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "VerificationEventCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalVerifiedAt().map(f -> "verifiedAt=" + f + ", ").orElse("") +
            optionalResult().map(f -> "result=" + f + ", ").orElse("") +
            optionalCredentialRef().map(f -> "credentialRef=" + f + ", ").orElse("") +
            optionalApiKeyId().map(f -> "apiKeyId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
