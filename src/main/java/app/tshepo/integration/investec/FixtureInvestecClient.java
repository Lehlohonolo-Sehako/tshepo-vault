package app.tshepo.integration.investec;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Returns deterministic SA fixture data so the full flow works in dev without real Investec credentials.
 * Data matches the design prototype: Thabo Mokoena, account •••• 4821, PattersonGroup salary ~R42 300/month.
 */
@Component
@Profile("!investec-live")
public class FixtureInvestecClient implements InvestecClient {

    private static final String FIXTURE_ACCOUNT_ID = "fixture-account-001";

    @Override
    public InvestecTokenPair exchangeAuthCode(String authCode, String redirectUri) {
        return new InvestecTokenPair("fixture-access-token", "fixture-refresh-token", 3600);
    }

    @Override
    public InvestecTokenPair refreshAccessToken(String refreshToken) {
        return new InvestecTokenPair("fixture-access-token-refreshed", "fixture-refresh-token-2", 3600);
    }

    @Override
    public List<InvestecAccount> getAccounts(String bearerToken) {
        return List.of(
            new InvestecAccount(FIXTURE_ACCOUNT_ID, "Thabo Mokoena", "10011234821", "Private Bank Account", true, LocalDate.of(2023, 1, 15))
        );
    }

    @Override
    public InvestecBalance getBalance(String bearerToken, String accountId) {
        return new InvestecBalance(FIXTURE_ACCOUNT_ID, new BigDecimal("148320.50"), new BigDecimal("148320.50"), "ZAR", LocalDate.now());
    }

    /**
     * Generates 12 months of fixture transactions.
     * Each month: one PattersonGroup salary credit (~R42 300) plus realistic debit activity.
     * Average monthly inflow ≥ R42 000 — satisfies the "avg_monthly_inflow_30k" claim.
     * No overdraft usage — satisfies the "no_overdraft_usage" claim.
     * Account tenure starts 2023-01 — satisfies the "account_tenure_6m" and "account_tenure_12m" claims.
     */
    @Override
    public List<InvestecTransaction> getTransactions(String bearerToken, String accountId, LocalDate fromDate, LocalDate toDate) {
        List<InvestecTransaction> transactions = new ArrayList<>();
        BigDecimal running = new BigDecimal("148320.50");
        LocalDate cursor = toDate.withDayOfMonth(1);

        for (int m = 0; m < 12; m++) {
            YearMonth ym = YearMonth.from(cursor);

            // Salary — always positive, 25th of month
            LocalDate salaryDate = ym.atDay(Math.min(25, ym.lengthOfMonth()));
            if (!salaryDate.isBefore(fromDate) && !salaryDate.isAfter(toDate)) {
                BigDecimal salary = new BigDecimal("42340.00");
                running = running.add(salary);
                transactions.add(
                    new InvestecTransaction(
                        InvestecTransaction.TransactionType.CREDIT,
                        salary,
                        "ZAR",
                        "PattersonGroup Consulting - Salary",
                        salaryDate,
                        running
                    )
                );
            }

            // Rent debit — 1st of month
            LocalDate rentDate = ym.atDay(1);
            if (!rentDate.isBefore(fromDate) && !rentDate.isAfter(toDate)) {
                BigDecimal rent = new BigDecimal("12500.00");
                running = running.subtract(rent);
                transactions.add(
                    new InvestecTransaction(
                        InvestecTransaction.TransactionType.DEBIT,
                        rent,
                        "ZAR",
                        "Landlord Property Management",
                        rentDate,
                        running
                    )
                );
            }

            // Groceries — 7th
            LocalDate groceryDate = ym.atDay(7);
            if (!groceryDate.isBefore(fromDate) && !groceryDate.isAfter(toDate)) {
                BigDecimal groceries = new BigDecimal("3200.00");
                running = running.subtract(groceries);
                transactions.add(
                    new InvestecTransaction(
                        InvestecTransaction.TransactionType.DEBIT,
                        groceries,
                        "ZAR",
                        "Checkers Superstore",
                        groceryDate,
                        running
                    )
                );
            }

            // Utilities — 15th
            LocalDate utilDate = ym.atDay(15);
            if (!utilDate.isBefore(fromDate) && !utilDate.isAfter(toDate)) {
                BigDecimal utils = new BigDecimal("1850.00");
                running = running.subtract(utils);
                transactions.add(
                    new InvestecTransaction(
                        InvestecTransaction.TransactionType.DEBIT,
                        utils,
                        "ZAR",
                        "City of Johannesburg Utilities",
                        utilDate,
                        running
                    )
                );
            }

            cursor = cursor.minusMonths(1);
        }

        return transactions;
    }
}
