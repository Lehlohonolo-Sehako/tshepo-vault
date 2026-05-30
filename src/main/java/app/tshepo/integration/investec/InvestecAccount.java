package app.tshepo.integration.investec;

import java.time.LocalDate;

public record InvestecAccount(
    String accountId,
    String accountName,
    String accountNumber,
    String productName,
    boolean kycCompliant,
    LocalDate accountOpenDate
) {}
