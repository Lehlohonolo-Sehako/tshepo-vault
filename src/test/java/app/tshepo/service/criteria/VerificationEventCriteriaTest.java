package app.tshepo.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class VerificationEventCriteriaTest {

    @Test
    void newVerificationEventCriteriaHasAllFiltersNullTest() {
        var verificationEventCriteria = new VerificationEventCriteria();
        assertThat(verificationEventCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void verificationEventCriteriaFluentMethodsCreatesFiltersTest() {
        var verificationEventCriteria = new VerificationEventCriteria();

        setAllFilters(verificationEventCriteria);

        assertThat(verificationEventCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void verificationEventCriteriaCopyCreatesNullFilterTest() {
        var verificationEventCriteria = new VerificationEventCriteria();
        var copy = verificationEventCriteria.copy();

        assertThat(verificationEventCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(verificationEventCriteria)
        );
    }

    @Test
    void verificationEventCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var verificationEventCriteria = new VerificationEventCriteria();
        setAllFilters(verificationEventCriteria);

        var copy = verificationEventCriteria.copy();

        assertThat(verificationEventCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(verificationEventCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var verificationEventCriteria = new VerificationEventCriteria();

        assertThat(verificationEventCriteria).hasToString("VerificationEventCriteria{}");
    }

    private static void setAllFilters(VerificationEventCriteria verificationEventCriteria) {
        verificationEventCriteria.id();
        verificationEventCriteria.verifiedAt();
        verificationEventCriteria.result();
        verificationEventCriteria.credentialRef();
        verificationEventCriteria.apiKeyId();
        verificationEventCriteria.distinct();
    }

    private static Condition<VerificationEventCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getVerifiedAt()) &&
                condition.apply(criteria.getResult()) &&
                condition.apply(criteria.getCredentialRef()) &&
                condition.apply(criteria.getApiKeyId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<VerificationEventCriteria> copyFiltersAre(
        VerificationEventCriteria copy,
        BiFunction<Object, Object, Boolean> condition
    ) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getVerifiedAt(), copy.getVerifiedAt()) &&
                condition.apply(criteria.getResult(), copy.getResult()) &&
                condition.apply(criteria.getCredentialRef(), copy.getCredentialRef()) &&
                condition.apply(criteria.getApiKeyId(), copy.getApiKeyId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
