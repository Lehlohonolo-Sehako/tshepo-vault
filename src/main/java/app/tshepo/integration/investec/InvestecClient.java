package app.tshepo.integration.investec;

import java.time.LocalDate;
import java.util.List;

public interface InvestecClient {
    InvestecTokenPair exchangeAuthCode(String authCode, String redirectUri);

    InvestecTokenPair refreshAccessToken(String refreshToken);

    List<InvestecAccount> getAccounts(String bearerToken);

    InvestecBalance getBalance(String bearerToken, String accountId);

    List<InvestecTransaction> getTransactions(String bearerToken, String accountId, LocalDate fromDate, LocalDate toDate);
}
