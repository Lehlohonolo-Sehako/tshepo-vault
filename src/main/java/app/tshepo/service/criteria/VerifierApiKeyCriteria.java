package app.tshepo.service.criteria;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link app.tshepo.domain.VerifierApiKey} entity. This class is used
 * in {@link app.tshepo.web.rest.VerifierApiKeyResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /verifier-api-keys?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class VerifierApiKeyCriteria implements Serializable, Criteria {

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter ownerLogin;

    private StringFilter label;

    private StringFilter keyHash;

    private BooleanFilter active;

    private LongFilter callCount;

    private InstantFilter createdAt;

    private Boolean distinct;

    public VerifierApiKeyCriteria() {}

    public VerifierApiKeyCriteria(VerifierApiKeyCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.ownerLogin = other.optionalOwnerLogin().map(StringFilter::copy).orElse(null);
        this.label = other.optionalLabel().map(StringFilter::copy).orElse(null);
        this.keyHash = other.optionalKeyHash().map(StringFilter::copy).orElse(null);
        this.active = other.optionalActive().map(BooleanFilter::copy).orElse(null);
        this.callCount = other.optionalCallCount().map(LongFilter::copy).orElse(null);
        this.createdAt = other.optionalCreatedAt().map(InstantFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public VerifierApiKeyCriteria copy() {
        return new VerifierApiKeyCriteria(this);
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

    public StringFilter getOwnerLogin() {
        return ownerLogin;
    }

    public Optional<StringFilter> optionalOwnerLogin() {
        return Optional.ofNullable(ownerLogin);
    }

    public StringFilter ownerLogin() {
        if (ownerLogin == null) {
            setOwnerLogin(new StringFilter());
        }
        return ownerLogin;
    }

    public void setOwnerLogin(StringFilter ownerLogin) {
        this.ownerLogin = ownerLogin;
    }

    public StringFilter getLabel() {
        return label;
    }

    public Optional<StringFilter> optionalLabel() {
        return Optional.ofNullable(label);
    }

    public StringFilter label() {
        if (label == null) {
            setLabel(new StringFilter());
        }
        return label;
    }

    public void setLabel(StringFilter label) {
        this.label = label;
    }

    public StringFilter getKeyHash() {
        return keyHash;
    }

    public Optional<StringFilter> optionalKeyHash() {
        return Optional.ofNullable(keyHash);
    }

    public StringFilter keyHash() {
        if (keyHash == null) {
            setKeyHash(new StringFilter());
        }
        return keyHash;
    }

    public void setKeyHash(StringFilter keyHash) {
        this.keyHash = keyHash;
    }

    public BooleanFilter getActive() {
        return active;
    }

    public Optional<BooleanFilter> optionalActive() {
        return Optional.ofNullable(active);
    }

    public BooleanFilter active() {
        if (active == null) {
            setActive(new BooleanFilter());
        }
        return active;
    }

    public void setActive(BooleanFilter active) {
        this.active = active;
    }

    public LongFilter getCallCount() {
        return callCount;
    }

    public Optional<LongFilter> optionalCallCount() {
        return Optional.ofNullable(callCount);
    }

    public LongFilter callCount() {
        if (callCount == null) {
            setCallCount(new LongFilter());
        }
        return callCount;
    }

    public void setCallCount(LongFilter callCount) {
        this.callCount = callCount;
    }

    public InstantFilter getCreatedAt() {
        return createdAt;
    }

    public Optional<InstantFilter> optionalCreatedAt() {
        return Optional.ofNullable(createdAt);
    }

    public InstantFilter createdAt() {
        if (createdAt == null) {
            setCreatedAt(new InstantFilter());
        }
        return createdAt;
    }

    public void setCreatedAt(InstantFilter createdAt) {
        this.createdAt = createdAt;
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
        final VerifierApiKeyCriteria that = (VerifierApiKeyCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(ownerLogin, that.ownerLogin) &&
            Objects.equals(label, that.label) &&
            Objects.equals(keyHash, that.keyHash) &&
            Objects.equals(active, that.active) &&
            Objects.equals(callCount, that.callCount) &&
            Objects.equals(createdAt, that.createdAt) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ownerLogin, label, keyHash, active, callCount, createdAt, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "VerifierApiKeyCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalOwnerLogin().map(f -> "ownerLogin=" + f + ", ").orElse("") +
            optionalLabel().map(f -> "label=" + f + ", ").orElse("") +
            optionalKeyHash().map(f -> "keyHash=" + f + ", ").orElse("") +
            optionalActive().map(f -> "active=" + f + ", ").orElse("") +
            optionalCallCount().map(f -> "callCount=" + f + ", ").orElse("") +
            optionalCreatedAt().map(f -> "createdAt=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
