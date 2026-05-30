package app.tshepo.integration.investec;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FixtureInvestecClientTest {

    private FixtureInvestecClient client;

    @BeforeEach
    void setUp() {
        client = new FixtureInvestecClient();
    }

    @Test
    void exchangeAuthCode_returnsFixtureToken() {
        InvestecTokenPair pair = client.exchangeAuthCode("any-code", "http://localhost/callback");
        assertThat(pair.accessToken()).isNotBlank();
        assertThat(pair.expiresIn()).isPositive();
    }

    @Test
    void getAccounts_returnsOneKycCompliantAccount() {
        List<InvestecAccount> accounts = client.getAccounts("token");
        assertThat(accounts).hasSize(1);
        assertThat(accounts.get(0).kycCompliant()).isTrue();
        assertThat(accounts.get(0).accountNumber()).endsWith("4821");
    }

    @Test
    void getBalance_returnsPositiveBalance() {
        InvestecBalance balance = client.getBalance("token", "fixture-account-001");
        assertThat(balance.currentBalance()).isGreaterThan(BigDecimal.ZERO);
        assertThat(balance.availableBalance()).isGreaterThan(BigDecimal.ZERO);
        assertThat(balance.currency()).isEqualTo("ZAR");
    }

    @Test
    void getTransactions_averageMonthlyInflowExceedsThirtyThousand() {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusMonths(12);
        List<InvestecTransaction> txns = client.getTransactions("token", "fixture-account-001", from, to);

        assertThat(txns).isNotEmpty();

        Map<String, BigDecimal> monthlyCredits = txns
            .stream()
            .filter(t -> t.type() == InvestecTransaction.TransactionType.CREDIT)
            .collect(
                Collectors.groupingBy(
                    t -> t.transactionDate().getYear() + "-" + t.transactionDate().getMonthValue(),
                    Collectors.reducing(BigDecimal.ZERO, InvestecTransaction::amount, BigDecimal::add)
                )
            );

        assertThat(monthlyCredits).isNotEmpty();
        double avgInflow = monthlyCredits.values().stream().mapToDouble(BigDecimal::doubleValue).average().orElse(0);

        assertThat(avgInflow).isGreaterThan(30_000.0);
    }

    @Test
    void getTransactions_noNegativeRunningBalance() {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusMonths(12);
        List<InvestecTransaction> txns = client.getTransactions("token", "fixture-account-001", from, to);

        txns.forEach(t ->
            assertThat(t.runningBalance())
                .as("Running balance must never go negative (no overdraft)")
                .isGreaterThanOrEqualTo(BigDecimal.ZERO)
        );
    }

    @Test
    void getTransactions_rawDataNotPersisted() {
        // Verifies the fixture returns in-memory records only — no side effects.
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusMonths(12);
        List<InvestecTransaction> first = client.getTransactions("token", "fixture-account-001", from, to);
        List<InvestecTransaction> second = client.getTransactions("token", "fixture-account-001", from, to);
        // Each call produces a fresh list — no shared mutable state.
        assertThat(first).isNotSameAs(second);
        assertThat(first).hasSameSizeAs(second);
    }
}
