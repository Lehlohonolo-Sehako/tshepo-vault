package app.tshepo.service;

import app.tshepo.domain.BankConnection;
import app.tshepo.domain.enumeration.BankConnectionStatus;
import app.tshepo.integration.investec.InvestecAccount;
import app.tshepo.integration.investec.InvestecClient;
import app.tshepo.integration.investec.InvestecTokenPair;
import app.tshepo.repository.BankConnectionRepository;
import app.tshepo.web.rest.vm.BankStatusResponse;
import app.tshepo.web.rest.vm.ComputedClaim;
import app.tshepo.web.rest.vm.GetAvailableClaims200Response;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class BankConnectService {

    private final BankConnectionRepository bankConnectionRepository;
    private final InvestecClient investecClient;
    private final PredicateService predicateService;
    private final ObjectMapper objectMapper;

    public BankConnectService(
        BankConnectionRepository bankConnectionRepository,
        InvestecClient investecClient,
        PredicateService predicateService,
        ObjectMapper objectMapper
    ) {
        this.bankConnectionRepository = bankConnectionRepository;
        this.investecClient = investecClient;
        this.predicateService = predicateService;
        this.objectMapper = objectMapper;
    }

    /** Called during OAuth callback where the token exchange has already been done. */
    public BankStatusResponse connectBankWithTokens(String holderLogin, InvestecTokenPair tokenPair) {
        List<InvestecAccount> accounts = investecClient.getAccounts(tokenPair.accessToken());
        return upsertConnection(holderLogin, tokenPair, accounts);
    }

    public BankStatusResponse connectBank(String holderLogin, String authCode, String redirectUri) {
        InvestecTokenPair tokenPair = investecClient.exchangeAuthCode(authCode, redirectUri);
        List<InvestecAccount> accounts = investecClient.getAccounts(tokenPair.accessToken());
        return upsertConnection(holderLogin, tokenPair, accounts);
    }

    private BankStatusResponse upsertConnection(String holderLogin, InvestecTokenPair tokenPair, List<InvestecAccount> accounts) {
        InvestecAccount account = accounts.isEmpty() ? null : accounts.get(0);
        String maskedAccount = account != null ? maskAccount(account.accountNumber()) : null;
        String accountType = account != null ? account.productName() : null;
        String accountId = account != null ? account.accountId() : "unknown";

        List<ComputedClaim> computedClaims = predicateService.computeAllClaims(tokenPair.accessToken(), accountId);
        String claimsJson = serializeClaims(computedClaims);

        BankConnection conn = bankConnectionRepository.findByHolderLogin(holderLogin).orElse(new BankConnection());
        conn.setHolderLogin(holderLogin);
        conn.setConnectedAt(Instant.now());
        conn.setStatus(BankConnectionStatus.CONNECTED);
        conn.setMaskedAccount(maskedAccount);
        conn.setAccountType(accountType);
        conn.setAccessToken(tokenPair.accessToken());
        conn.setPreComputedClaimsJson(claimsJson);
        bankConnectionRepository.save(conn);

        return toBankStatusResponse(conn, true);
    }

    @Transactional(readOnly = true)
    public BankStatusResponse getStatus(String holderLogin) {
        Optional<BankConnection> opt = bankConnectionRepository.findByHolderLogin(holderLogin);
        if (opt.isEmpty()) {
            return new BankStatusResponse().connected(false);
        }
        return toBankStatusResponse(opt.get(), opt.get().getStatus() == BankConnectionStatus.CONNECTED);
    }

    @Transactional(readOnly = true)
    public GetAvailableClaims200Response getAvailableClaims(String holderLogin) {
        BankConnection conn = bankConnectionRepository
            .findByHolderLogin(holderLogin)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "No bank account connected"));

        if (conn.getStatus() != BankConnectionStatus.CONNECTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bank connection is not active");
        }

        List<ComputedClaim> claims = deserializeClaims(conn.getPreComputedClaimsJson());
        GetAvailableClaims200Response response = new GetAvailableClaims200Response();
        response.setClaims(claims);
        return response;
    }

    private static BankStatusResponse toBankStatusResponse(BankConnection conn, boolean connected) {
        BankStatusResponse r = new BankStatusResponse();
        r.setConnected(connected);
        r.setMaskedAccount(conn.getMaskedAccount());
        if (conn.getConnectedAt() != null) {
            r.setConnectedAt(OffsetDateTime.ofInstant(conn.getConnectedAt(), ZoneOffset.UTC));
        }
        if (conn.getStatus() != null) {
            r.setStatus(BankStatusResponse.StatusEnum.fromValue(conn.getStatus().name()));
        }
        return r;
    }

    private static String maskAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) return accountNumber;
        return "•••• " + accountNumber.substring(accountNumber.length() - 4);
    }

    private String serializeClaims(List<ComputedClaim> claims) {
        try {
            return objectMapper.writeValueAsString(claims);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<ComputedClaim> deserializeClaims(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
