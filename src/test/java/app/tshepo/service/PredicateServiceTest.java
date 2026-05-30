package app.tshepo.service;

import static org.assertj.core.api.Assertions.assertThat;

import app.tshepo.integration.investec.FixtureInvestecClient;
import app.tshepo.integration.investec.InvestecClient;
import app.tshepo.web.rest.vm.ClaimThreshold;
import app.tshepo.web.rest.vm.ClaimType;
import app.tshepo.web.rest.vm.ComputedClaim;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PredicateServiceTest {

    private PredicateService service;

    @BeforeEach
    void setUp() {
        InvestecClient fixture = new FixtureInvestecClient();
        service = new PredicateService(fixture);
    }

    @Test
    void inflow_meetsThresholdFor30k() {
        ClaimThreshold ct = threshold(ClaimType.INFLOW, "GTE", 30_000, "ZAR", 6);
        List<ComputedClaim> results = service.computeClaims("token", "fixture-account-001", List.of(ct));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getMet()).isTrue();
        assertThat(results.get(0).getBasis()).contains("Avg");
    }

    @Test
    void inflow_failsHighThreshold() {
        ClaimThreshold ct = threshold(ClaimType.INFLOW, "GTE", 200_000, "ZAR", 6);
        List<ComputedClaim> results = service.computeClaims("token", "fixture-account-001", List.of(ct));

        assertThat(results.get(0).getMet()).isFalse();
    }

    @Test
    void balance_meetsThresholdFor50k() {
        ClaimThreshold ct = threshold(ClaimType.BALANCE, "GTE", 50_000, "ZAR", 1);
        List<ComputedClaim> results = service.computeClaims("token", "fixture-account-001", List.of(ct));

        assertThat(results.get(0).getMet()).isTrue();
    }

    @Test
    void tenure_meetsThresholdFor6Months() {
        ClaimThreshold ct = threshold(ClaimType.TENURE, "GTE", 6, null, 12);
        List<ComputedClaim> results = service.computeClaims("token", "fixture-account-001", List.of(ct));

        assertThat(results.get(0).getMet()).isTrue();
    }

    @Test
    void noOverdraft_meetsForFixtureData() {
        ClaimThreshold ct = threshold(ClaimType.NO_OVERDRAFT, "EQ", 1, null, 6);
        List<ComputedClaim> results = service.computeClaims("token", "fixture-account-001", List.of(ct));

        assertThat(results.get(0).getMet()).isTrue();
    }

    @Test
    void salaryContinuity_meetsFor3Months() {
        ClaimThreshold ct = threshold(ClaimType.SALARY_CONTINUITY, "GTE", 3, null, 6);
        List<ComputedClaim> results = service.computeClaims("token", "fixture-account-001", List.of(ct));

        assertThat(results.get(0).getMet()).isTrue();
    }

    @Test
    void computeAllClaims_returnsAllSixPredicates() {
        List<ComputedClaim> all = service.computeAllClaims("token", "fixture-account-001");
        assertThat(all).hasSize(6);
        assertThat(all).allMatch(c -> c.getMet() != null);
    }

    @Test
    void multipleClaims_allEvaluatedIndependently() {
        List<ClaimThreshold> claims = List.of(
            threshold(ClaimType.INFLOW, "GTE", 30_000, "ZAR", 6),
            threshold(ClaimType.BALANCE, "GTE", 50_000, "ZAR", 1),
            threshold(ClaimType.NO_OVERDRAFT, "EQ", 1, null, 6)
        );
        List<ComputedClaim> results = service.computeClaims("token", "fixture-account-001", claims);

        assertThat(results).hasSize(3);
        assertThat(results).allMatch(c -> Boolean.TRUE.equals(c.getMet()));
    }

    private static ClaimThreshold threshold(ClaimType type, String op, double threshold, String currency, int months) {
        ClaimThreshold ct = new ClaimThreshold();
        ct.setType(type);
        ct.setOperator(ClaimThreshold.OperatorEnum.fromValue(op));
        ct.setThreshold(BigDecimal.valueOf(threshold));
        ct.setCurrency(currency);
        ct.setPeriodMonths(months);
        return ct;
    }
}
