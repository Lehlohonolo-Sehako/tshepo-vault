package app.tshepo.service;

import app.tshepo.integration.investec.InvestecAccount;
import app.tshepo.integration.investec.InvestecBalance;
import app.tshepo.integration.investec.InvestecClient;
import app.tshepo.integration.investec.InvestecTransaction;
import app.tshepo.web.rest.vm.ClaimThreshold;
import app.tshepo.web.rest.vm.ClaimType;
import app.tshepo.web.rest.vm.ComputedClaim;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

/**
 * Computes claim predicates from live Investec data entirely in memory.
 * No transaction data is stored or logged — only the boolean result survives.
 */
@Service
public class PredicateService {

    private static final Logger LOG = LoggerFactory.getLogger(PredicateService.class);

    private final InvestecClient investecClient;

    public PredicateService(InvestecClient investecClient) {
        this.investecClient = investecClient;
    }

    /**
     * Evaluates all requested claims against the holder's bank data.
     * Raw transaction data is discarded immediately after evaluation.
     *
     * @param bearerToken Investec bearer token (ignored by fixture client)
     * @param accountId   account to analyse
     * @param requested   list of claim thresholds to evaluate
     * @return computed claim results (basis shown to holder only — never persisted)
     */
    public List<ComputedClaim> computeClaims(String bearerToken, String accountId, List<ClaimThreshold> requested) {
        // Pull raw data — discarded at end of this method scope
        int maxPeriod = requested
            .stream()
            .mapToInt(c -> c.getPeriodMonths() != null ? c.getPeriodMonths() : 6)
            .max()
            .orElse(12);

        LocalDate toDate = LocalDate.now();
        LocalDate fromDate = toDate.minusMonths(maxPeriod);

        List<InvestecTransaction> txns;
        InvestecBalance balance;
        List<InvestecAccount> accounts;
        try {
            txns = investecClient.getTransactions(bearerToken, accountId, fromDate, toDate);
        } catch (RestClientException e) {
            LOG.warn("Could not fetch transactions for account {}: {}", accountId, e.getMessage());
            txns = List.of();
        }
        try {
            balance = investecClient.getBalance(bearerToken, accountId);
        } catch (RestClientException e) {
            LOG.warn("Could not fetch balance for account {}: {}", accountId, e.getMessage());
            balance = null;
        }
        try {
            accounts = investecClient.getAccounts(bearerToken);
        } catch (RestClientException e) {
            LOG.warn("Could not fetch accounts: {}", e.getMessage());
            accounts = List.of();
        }
        InvestecAccount account = accounts
            .stream()
            .filter(a -> a.accountId().equals(accountId))
            .findFirst()
            .orElse(accounts.isEmpty() ? null : accounts.get(0));

        List<ComputedClaim> results = new ArrayList<>();
        for (ClaimThreshold req : requested) {
            results.add(evaluate(req, txns, balance, account, toDate));
        }

        // txns, balance, account go out of scope — GC'd immediately
        return results;
    }

    /**
     * Computes all supported claims for the "available claims" endpoint.
     */
    public List<ComputedClaim> computeAllClaims(String bearerToken, String accountId) {
        List<ClaimThreshold> allClaims = List.of(
            threshold(ClaimType.INFLOW, "GTE", new BigDecimal("30000"), "ZAR", 6),
            threshold(ClaimType.BALANCE, "GTE", new BigDecimal("50000"), "ZAR", 1),
            threshold(ClaimType.TENURE, "GTE", new BigDecimal("6"), null, 12),
            threshold(ClaimType.TENURE, "GTE", new BigDecimal("12"), null, 12),
            threshold(ClaimType.SALARY_CONTINUITY, "GTE", new BigDecimal("3"), null, 6),
            threshold(ClaimType.NO_OVERDRAFT, "EQ", BigDecimal.ONE, null, 6)
        );
        return computeClaims(bearerToken, accountId, allClaims);
    }

    private ComputedClaim evaluate(
        ClaimThreshold req,
        List<InvestecTransaction> txns,
        InvestecBalance balance,
        InvestecAccount account,
        LocalDate today
    ) {
        int period = req.getPeriodMonths() != null ? req.getPeriodMonths() : 6;
        LocalDate windowStart = today.minusMonths(period);

        ComputedClaim result = new ComputedClaim()
            .type(req.getType())
            .operator(req.getOperator() != null ? req.getOperator().getValue() : "GTE")
            .threshold(req.getThreshold())
            .currency(req.getCurrency());

        return switch (req.getType()) {
            case INFLOW -> evaluateInflow(result, txns, windowStart, today, req.getThreshold(), period);
            case BALANCE -> evaluateBalance(result, balance, req.getThreshold());
            case TENURE -> evaluateTenure(result, account, txns, today, req.getThreshold());
            case SALARY_CONTINUITY -> evaluateSalaryContinuity(result, txns, windowStart, req.getThreshold(), period);
            case NO_OVERDRAFT -> evaluateNoOverdraft(result, txns, windowStart);
        };
    }

    private ComputedClaim evaluateInflow(
        ComputedClaim result,
        List<InvestecTransaction> txns,
        LocalDate from,
        LocalDate to,
        BigDecimal threshold,
        int periodMonths
    ) {
        Map<YearMonth, BigDecimal> monthlyCredits = txns
            .stream()
            .filter(t -> !t.transactionDate().isBefore(from) && !t.transactionDate().isAfter(to))
            .filter(t -> t.type() == InvestecTransaction.TransactionType.CREDIT)
            .collect(
                Collectors.groupingBy(
                    t -> YearMonth.from(t.transactionDate()),
                    Collectors.reducing(BigDecimal.ZERO, InvestecTransaction::amount, BigDecimal::add)
                )
            );

        if (monthlyCredits.isEmpty()) {
            return result.met(false).basis("No credit transactions found in window");
        }

        BigDecimal totalCredits = monthlyCredits.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avg = totalCredits.divide(BigDecimal.valueOf(periodMonths), 2, RoundingMode.HALF_UP);
        boolean met = avg.compareTo(threshold) >= 0;
        return result.met(met).basis(String.format("Avg %.2f ZAR/month over %d months", avg, periodMonths));
    }

    private ComputedClaim evaluateBalance(ComputedClaim result, InvestecBalance balance, BigDecimal threshold) {
        if (balance == null) return result.met(false).basis("Balance unavailable");
        boolean met = balance.currentBalance().compareTo(threshold) >= 0;
        return result.met(met).basis(String.format("Current balance %.2f ZAR", balance.currentBalance()));
    }

    private ComputedClaim evaluateTenure(
        ComputedClaim result,
        InvestecAccount account,
        List<InvestecTransaction> txns,
        LocalDate today,
        BigDecimal thresholdMonths
    ) {
        LocalDate openDate = null;
        if (account != null && account.accountOpenDate() != null) {
            openDate = account.accountOpenDate();
        } else if (!txns.isEmpty()) {
            openDate = txns.stream().map(InvestecTransaction::transactionDate).min(LocalDate::compareTo).orElse(null);
        }

        if (openDate == null) return result.met(false).basis("Account open date unknown");

        long months = ChronoUnit.MONTHS.between(openDate, today);
        boolean met = months >= thresholdMonths.longValue();
        return result.met(met).basis(String.format("Account opened %s (%d months ago)", openDate, months));
    }

    private ComputedClaim evaluateSalaryContinuity(
        ComputedClaim result,
        List<InvestecTransaction> txns,
        LocalDate from,
        BigDecimal thresholdMonths,
        int periodMonths
    ) {
        BigDecimal salaryFloor = new BigDecimal("20000");
        Set<YearMonth> monthsWithSalary = txns
            .stream()
            .filter(t -> !t.transactionDate().isBefore(from))
            .filter(t -> t.type() == InvestecTransaction.TransactionType.CREDIT)
            .filter(t -> t.amount().compareTo(salaryFloor) >= 0)
            .map(t -> YearMonth.from(t.transactionDate()))
            .collect(Collectors.toSet());

        long consecutiveMonths = 0;
        long maxConsecutive = 0;
        YearMonth cursor = YearMonth.from(from);
        YearMonth end = YearMonth.now();
        while (!cursor.isAfter(end)) {
            if (monthsWithSalary.contains(cursor)) {
                consecutiveMonths++;
                maxConsecutive = Math.max(maxConsecutive, consecutiveMonths);
            } else {
                consecutiveMonths = 0;
            }
            cursor = cursor.plusMonths(1);
        }

        boolean met = maxConsecutive >= thresholdMonths.longValue();
        return result.met(met).basis(String.format("%d consecutive months with large credit", maxConsecutive));
    }

    private ComputedClaim evaluateNoOverdraft(ComputedClaim result, List<InvestecTransaction> txns, LocalDate from) {
        boolean overdraftUsed = txns
            .stream()
            .filter(t -> !t.transactionDate().isBefore(from))
            .anyMatch(t -> t.runningBalance() != null && t.runningBalance().compareTo(BigDecimal.ZERO) < 0);
        return result.met(!overdraftUsed).basis(overdraftUsed ? "Overdraft detected in window" : "No overdraft in window");
    }

    private static ClaimThreshold threshold(ClaimType type, String operator, BigDecimal threshold, String currency, int periodMonths) {
        ClaimThreshold ct = new ClaimThreshold();
        ct.setType(type);
        ct.setOperator(ClaimThreshold.OperatorEnum.fromValue(operator));
        ct.setThreshold(threshold);
        ct.setCurrency(currency);
        ct.setPeriodMonths(periodMonths);
        return ct;
    }
}
