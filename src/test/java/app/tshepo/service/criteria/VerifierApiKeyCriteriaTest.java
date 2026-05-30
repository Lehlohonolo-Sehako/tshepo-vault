package app.tshepo.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class VerifierApiKeyCriteriaTest {

    @Test
    void newVerifierApiKeyCriteriaHasAllFiltersNullTest() {
        var verifierApiKeyCriteria = new VerifierApiKeyCriteria();
        assertThat(verifierApiKeyCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void verifierApiKeyCriteriaFluentMethodsCreatesFiltersTest() {
        var verifierApiKeyCriteria = new VerifierApiKeyCriteria();

        setAllFilters(verifierApiKeyCriteria);

        assertThat(verifierApiKeyCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void verifierApiKeyCriteriaCopyCreatesNullFilterTest() {
        var verifierApiKeyCriteria = new VerifierApiKeyCriteria();
        var copy = verifierApiKeyCriteria.copy();

        assertThat(verifierApiKeyCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(verifierApiKeyCriteria)
        );
    }

    @Test
    void verifierApiKeyCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var verifierApiKeyCriteria = new VerifierApiKeyCriteria();
        setAllFilters(verifierApiKeyCriteria);

        var copy = verifierApiKeyCriteria.copy();

        assertThat(verifierApiKeyCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(verifierApiKeyCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var verifierApiKeyCriteria = new VerifierApiKeyCriteria();

        assertThat(verifierApiKeyCriteria).hasToString("VerifierApiKeyCriteria{}");
    }

    private static void setAllFilters(VerifierApiKeyCriteria verifierApiKeyCriteria) {
        verifierApiKeyCriteria.id();
        verifierApiKeyCriteria.ownerLogin();
        verifierApiKeyCriteria.label();
        verifierApiKeyCriteria.keyHash();
        verifierApiKeyCriteria.active();
        verifierApiKeyCriteria.callCount();
        verifierApiKeyCriteria.createdAt();
        verifierApiKeyCriteria.distinct();
    }

    private static Condition<VerifierApiKeyCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getOwnerLogin()) &&
                condition.apply(criteria.getLabel()) &&
                condition.apply(criteria.getKeyHash()) &&
                condition.apply(criteria.getActive()) &&
                condition.apply(criteria.getCallCount()) &&
                condition.apply(criteria.getCreatedAt()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<VerifierApiKeyCriteria> copyFiltersAre(
        VerifierApiKeyCriteria copy,
        BiFunction<Object, Object, Boolean> condition
    ) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getOwnerLogin(), copy.getOwnerLogin()) &&
                condition.apply(criteria.getLabel(), copy.getLabel()) &&
                condition.apply(criteria.getKeyHash(), copy.getKeyHash()) &&
                condition.apply(criteria.getActive(), copy.getActive()) &&
                condition.apply(criteria.getCallCount(), copy.getCallCount()) &&
                condition.apply(criteria.getCreatedAt(), copy.getCreatedAt()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
