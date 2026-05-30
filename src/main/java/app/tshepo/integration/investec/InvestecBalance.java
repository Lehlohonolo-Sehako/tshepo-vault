package app.tshepo.integration.investec;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvestecBalance(String accountId, BigDecimal currentBalance, BigDecimal availableBalance, String currency, LocalDate date) {}
