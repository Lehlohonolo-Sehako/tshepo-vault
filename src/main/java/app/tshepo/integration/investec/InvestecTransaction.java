package app.tshepo.integration.investec;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvestecTransaction(
    TransactionType type,
    BigDecimal amount,
    String currency,
    String description,
    LocalDate transactionDate,
    BigDecimal runningBalance
) {
    public enum TransactionType {
        CREDIT,
        DEBIT,
    }
}
