package app.tshepo.integration.investec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
@Profile("investec-live")
public class LiveInvestecClient implements InvestecClient {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final RestClient restClient;
    private final RestClient tokenClient;
    private final InvestecProperties props;

    public LiveInvestecClient(InvestecProperties props) {
        this.props = props;
        // API calls: force HTTP/1.1 (local sandbox simulator does not support HTTP/2)
        JdkClientHttpRequestFactory http11 = new JdkClientHttpRequestFactory(
            HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build()
        );
        this.restClient = RestClient.builder().baseUrl(props.getApiBase()).requestFactory(http11).build();

        // Token endpoint: read raw string to bypass content-type negotiation.
        // Some sandbox implementations return application/octet-stream instead of
        // application/json, which confuses Spring's default message converters.
        SimpleClientHttpRequestFactory simple = new SimpleClientHttpRequestFactory();
        simple.setConnectTimeout(10000);
        simple.setReadTimeout(15000);
        this.tokenClient = RestClient.builder().baseUrl(props.getTokenUri()).requestFactory(simple).build();
    }

    @Override
    public InvestecTokenPair exchangeAuthCode(String authCode, String redirectUri) {
        return fetchClientCredentialsToken();
    }

    @Override
    public InvestecTokenPair refreshAccessToken(String refreshToken) {
        return fetchClientCredentialsToken();
    }

    private InvestecTokenPair fetchClientCredentialsToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("scope", "accounts");
        // Read as byte[] — ByteArrayHttpMessageConverter supports */* so it works
        // regardless of whether the server returns application/json or application/octet-stream.
        byte[] bytes = tokenClient
            .post()
            .uri("")
            .header(HttpHeaders.AUTHORIZATION, basicAuth())
            .header("x-api-key", props.getApiKey())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(byte[].class);
        try {
            TokenResponse resp = MAPPER.readValue(bytes, TokenResponse.class);
            return new InvestecTokenPair(resp.accessToken(), resp.refreshToken(), resp.expiresIn());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse token response: " + new String(bytes, StandardCharsets.UTF_8), e);
        }
    }

    @Override
    public List<InvestecAccount> getAccounts(String bearerToken) {
        AccountsEnvelope env = restClient
            .get()
            .uri("/za/pb/v1/accounts")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
            .header("x-api-key", props.getApiKey())
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
                    Boolean.TRUE.equals(a.kycCompliant()),
                    null
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
            .header("x-api-key", props.getApiKey())
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
            .header("x-api-key", props.getApiKey())
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

    // --- JSON mapping records ---

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
    private record AccountJson(String accountId, String accountName, String accountNumber, String productName, Boolean kycCompliant) {}

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
