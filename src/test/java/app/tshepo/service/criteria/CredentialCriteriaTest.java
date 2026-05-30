package app.tshepo.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class CredentialCriteriaTest {

    @Test
    void newCredentialCriteriaHasAllFiltersNullTest() {
        var credentialCriteria = new CredentialCriteria();
        assertThat(credentialCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void credentialCriteriaFluentMethodsCreatesFiltersTest() {
        var credentialCriteria = new CredentialCriteria();

        setAllFilters(credentialCriteria);

        assertThat(credentialCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void credentialCriteriaCopyCreatesNullFilterTest() {
        var credentialCriteria = new CredentialCriteria();
        var copy = credentialCriteria.copy();

        assertThat(credentialCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(credentialCriteria)
        );
    }

    @Test
    void credentialCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var credentialCriteria = new CredentialCriteria();
        setAllFilters(credentialCriteria);

        var copy = credentialCriteria.copy();

        assertThat(credentialCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(credentialCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var credentialCriteria = new CredentialCriteria();

        assertThat(credentialCriteria).hasToString("CredentialCriteria{}");
    }

    private static void setAllFilters(CredentialCriteria credentialCriteria) {
        credentialCriteria.id();
        credentialCriteria.holderLogin();
        credentialCriteria.title();
        credentialCriteria.purpose();
        credentialCriteria.status();
        credentialCriteria.issuedAt();
        credentialCriteria.expiresAt();
        credentialCriteria.issuerDid();
        credentialCriteria.claimsSummary();
        credentialCriteria.vcRef();
        credentialCriteria.claimsId();
        credentialCriteria.distinct();
    }

    private static Condition<CredentialCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getHolderLogin()) &&
                condition.apply(criteria.getTitle()) &&
                condition.apply(criteria.getPurpose()) &&
                condition.apply(criteria.getStatus()) &&
                condition.apply(criteria.getIssuedAt()) &&
                condition.apply(criteria.getExpiresAt()) &&
                condition.apply(criteria.getIssuerDid()) &&
                condition.apply(criteria.getClaimsSummary()) &&
                condition.apply(criteria.getVcRef()) &&
                condition.apply(criteria.getClaimsId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<CredentialCriteria> copyFiltersAre(CredentialCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getHolderLogin(), copy.getHolderLogin()) &&
                condition.apply(criteria.getTitle(), copy.getTitle()) &&
                condition.apply(criteria.getPurpose(), copy.getPurpose()) &&
                condition.apply(criteria.getStatus(), copy.getStatus()) &&
                condition.apply(criteria.getIssuedAt(), copy.getIssuedAt()) &&
                condition.apply(criteria.getExpiresAt(), copy.getExpiresAt()) &&
                condition.apply(criteria.getIssuerDid(), copy.getIssuerDid()) &&
                condition.apply(criteria.getClaimsSummary(), copy.getClaimsSummary()) &&
                condition.apply(criteria.getVcRef(), copy.getVcRef()) &&
                condition.apply(criteria.getClaimsId(), copy.getClaimsId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
