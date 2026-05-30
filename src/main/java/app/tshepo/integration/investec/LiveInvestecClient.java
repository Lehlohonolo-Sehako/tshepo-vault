package app.tshepo.integration.investec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
@Profile("investec-live")
public class LiveInvestecClient implements InvestecClient {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final RestClient restClient;
    private final InvestecProperties props;

    public LiveInvestecClient(InvestecProperties props) {
        this.props = props;
        this.restClient = RestClient.builder().baseUrl(props.getApiBase()).build();
    }

    @Override
    public InvestecTokenPair exchangeAuthCode(String authCode, String redirectUri) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", authCode);
        form.add("redirect_uri", redirectUri);
        TokenResponse resp = restClient
            .post()
            .uri(props.getTokenUri())
            .header(HttpHeaders.AUTHORIZATION, basicAuth())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(TokenResponse.class);
        return new InvestecTokenPair(resp.accessToken(), resp.refreshToken(), resp.expiresIn());
    }

    @Override
    public InvestecTokenPair refreshAccessToken(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", refreshToken);
        TokenResponse resp = restClient
            .post()
            .uri(props.getTokenUri())
            .header(HttpHeaders.AUTHORIZATION, basicAuth())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(TokenResponse.class);
        return new InvestecTokenPair(resp.accessToken(), resp.refreshToken(), resp.expiresIn());
    }

    @Override
    public List<InvestecAccount> getAccounts(String bearerToken) {
        AccountsEnvelope env = restClient
            .get()
            .uri("/za/pb/v1/accounts")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
            .retrieve()
            .body(AccountsEnvelope.class);
        return env
            .data()
            .accounts()
            .stream()
            .map(a ->
                new InvestecAccount(
                    a.accountId(),
                    a.accountName(),
                    a.accountNumber(),
                    a.productName(),
                    a.kycCompliant(),
                    null // Investec accounts API does not surface open date; tenure computed from transactions
                )
            )
            .toList();
    }

    @Override
    public InvestecBalance getBalance(String bearerToken, String accountId) {
        BalanceEnvelope env = restClient
            .get()
            .uri("/za/pb/v1/accounts/{id}/balance", accountId)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
            .retrieve()
            .body(BalanceEnvelope.class);
        BalanceData d = env.data();
        return new InvestecBalance(accountId, d.currentBalance(), d.availableBalance(), d.currency(), LocalDate.now());
    }

    @Override
    public List<InvestecTransaction> getTransactions(String bearerToken, String accountId, LocalDate fromDate, LocalDate toDate) {
        TransactionsEnvelope env = restClient
            .get()
            .uri(
                "/za/pb/v1/accounts/{id}/transactions?fromDate={from}&toDate={to}",
                accountId,
                fromDate.format(DATE_FMT),
                toDate.format(DATE_FMT)
            )
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
            .retrieve()
            .body(TransactionsEnvelope.class);
        return env
            .data()
            .transactions()
            .stream()
            .map(t ->
                new InvestecTransaction(
                    "CREDIT".equalsIgnoreCase(t.type())
                        ? InvestecTransaction.TransactionType.CREDIT
                        : InvestecTransaction.TransactionType.DEBIT,
                    t.amount(),
                    t.currency(),
                    t.description(),
                    LocalDate.parse(t.transactionDate(), DATE_FMT),
                    t.runningBalance()
                )
            )
            .toList();
    }

    private String basicAuth() {
        String creds = props.getClientId() + ":" + props.getClientSecret();
        return "Basic " + Base64.getEncoder().encodeToString(creds.getBytes(StandardCharsets.UTF_8));
    }

    // --- JSON mapping records (package-private, used only here) ---

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("expires_in") int expiresIn
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AccountsEnvelope(AccountsData data) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AccountsData(List<AccountJson> accounts) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AccountJson(String accountId, String accountName, String accountNumber, String productName, boolean kycCompliant) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record BalanceEnvelope(BalanceData data) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record BalanceData(BigDecimal currentBalance, BigDecimal availableBalance, String currency) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TransactionsEnvelope(TransactionsData data) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TransactionsData(List<TransactionJson> transactions) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TransactionJson(
        String type,
        BigDecimal amount,
        String currency,
        String description,
        String transactionDate,
        BigDecimal runningBalance
    ) {}
}
