package app.tshepo.web.rest;

import app.tshepo.domain.User;
import app.tshepo.integration.investec.InvestecAccount;
import app.tshepo.integration.investec.InvestecClient;
import app.tshepo.integration.investec.InvestecProperties;
import app.tshepo.integration.investec.InvestecTokenPair;
import app.tshepo.security.AuthoritiesConstants;
import app.tshepo.security.SecurityUtils;
import app.tshepo.service.HolderProvisioningService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpClientErrorException;

@Controller
@RequestMapping("/api/auth/investec")
public class InvestecOAuthController {

    private static final Logger LOG = LoggerFactory.getLogger(InvestecOAuthController.class);

    private final InvestecClient investecClient;
    private final InvestecProperties investecProperties;
    private final HolderProvisioningService provisioningService;
    private final JwtEncoder jwtEncoder;

    public InvestecOAuthController(
        InvestecClient investecClient,
        InvestecProperties investecProperties,
        HolderProvisioningService provisioningService,
        JwtEncoder jwtEncoder
    ) {
        this.investecClient = investecClient;
        this.investecProperties = investecProperties;
        this.provisioningService = provisioningService;
        this.jwtEncoder = jwtEncoder;
    }

    /**
     * Single-step connect: fetches a token (client_credentials for live, fixture for dev),
     * provisions the user + bank connection in one committed transaction, then issues our JWT.
     */
    @GetMapping("/authorize")
    public void authorize(HttpServletResponse response) throws IOException {
        try {
            InvestecTokenPair tokens = investecClient.exchangeAuthCode("", "");
            List<InvestecAccount> accounts = investecClient.getAccounts(tokens.accessToken());
            InvestecAccount primary = accounts.isEmpty() ? null : accounts.get(0);
            User user = provisioningService.provisionAndConnect(primary, tokens);
            String jwt = issueJwt(user);
            response.sendRedirect("/oauth-callback?token=" + encode(jwt));
        } catch (Exception ex) {
            LOG.warn("Investec connect failed: {}", ex.getMessage());
            response.sendRedirect("/connect?error=" + errorCode(ex));
        }
    }

    private static String errorCode(Throwable ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        if (cause instanceof ConnectException) return "connection_refused";
        if (cause instanceof SocketTimeoutException) return "timeout";
        if (ex instanceof HttpClientErrorException.Unauthorized) return "auth_failed";
        if (ex.getMessage() != null && ex.getMessage().contains("timeout")) return "timeout";
        if (ex.getMessage() != null && ex.getMessage().contains("Connection refused")) return "connection_refused";
        return "server_error";
    }

    private String issueJwt(User user) {
        Instant now = Instant.now();
        JwsHeader jwsHeader = JwsHeader.with(SecurityUtils.JWT_ALGORITHM).build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuedAt(now)
            .expiresAt(now.plus(86400, ChronoUnit.SECONDS))
            .subject(user.getLogin())
            .claim(SecurityUtils.AUTHORITIES_CLAIM, AuthoritiesConstants.USER)
            .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
