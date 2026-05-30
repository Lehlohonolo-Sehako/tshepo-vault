package app.tshepo.web.rest;

import app.tshepo.domain.Authority;
import app.tshepo.domain.User;
import app.tshepo.integration.investec.InvestecAccount;
import app.tshepo.integration.investec.InvestecClient;
import app.tshepo.integration.investec.InvestecProperties;
import app.tshepo.integration.investec.InvestecTokenPair;
import app.tshepo.repository.AuthorityRepository;
import app.tshepo.repository.UserRepository;
import app.tshepo.security.AuthoritiesConstants;
import app.tshepo.security.SecurityUtils;
import app.tshepo.service.BankConnectService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tech.jhipster.config.JHipsterConstants;

/**
 * Handles the Investec OAuth2 authorisation-code flow.
 *
 * GET /api/auth/investec/authorize  — sends the browser to Investec (or
 *   straight to the callback in fixture/dev mode).
 * GET /api/auth/investec/callback   — receives the code, exchanges it for
 *   tokens, provisions the user and bank connection, issues a JWT, and
 *   redirects the SPA to /oauth-callback?token=…
 */
@Controller
@RequestMapping("/api/auth/investec")
public class InvestecOAuthController {

    private final InvestecClient investecClient;
    private final InvestecProperties investecProperties;
    private final BankConnectService bankConnectService;
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final Environment env;

    public InvestecOAuthController(
        InvestecClient investecClient,
        InvestecProperties investecProperties,
        BankConnectService bankConnectService,
        UserRepository userRepository,
        AuthorityRepository authorityRepository,
        PasswordEncoder passwordEncoder,
        JwtEncoder jwtEncoder,
        Environment env
    ) {
        this.investecClient = investecClient;
        this.investecProperties = investecProperties;
        this.bankConnectService = bankConnectService;
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.env = env;
    }

    /** Step 1 — redirect the browser to Investec (or bypass in dev/fixture mode). */
    @GetMapping("/authorize")
    public void authorize(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (isFixtureMode()) {
            // Dev shortcut: skip Investec, jump straight to callback with a fixture code.
            response.sendRedirect(baseUrl(request) + "/api/auth/investec/callback?code=fixture-code");
            return;
        }

        String state = UUID.randomUUID().toString();
        String redirectUri = baseUrl(request) + "/api/auth/investec/callback";
        String authUrl =
            investecProperties.getAuthorizeUri() +
            "?response_type=code" +
            "&client_id=" +
            encode(investecProperties.getClientId()) +
            "&redirect_uri=" +
            encode(redirectUri) +
            "&scope=accounts" +
            "&state=" +
            state;

        response.sendRedirect(authUrl);
    }

    /** Step 2 — Investec (or fixture) posts the code back here. */
    @GetMapping("/callback")
    @Transactional
    public void callback(
        @RequestParam String code,
        @RequestParam(required = false) String state,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException {
        String redirectUri = baseUrl(request) + "/api/auth/investec/callback";

        // Exchange code → tokens (single call; BankConnectService reuses them via connectBankWithTokens)
        InvestecTokenPair tokens = investecClient.exchangeAuthCode(code, redirectUri);

        // Identify the holder
        List<InvestecAccount> accounts = investecClient.getAccounts(tokens.accessToken());
        InvestecAccount primary = accounts.isEmpty() ? null : accounts.get(0);

        // Provision user (first-time: create; returning: fetch)
        User user = findOrCreateUser(primary);

        // Upsert BankConnection (pre-computes claims, stores access token)
        bankConnectService.connectBankWithTokens(user.getLogin(), tokens);

        // Issue our JWT and hand it to the SPA
        String jwt = issueJwt(user);
        response.sendRedirect("/oauth-callback?token=" + encode(jwt));
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private User findOrCreateUser(InvestecAccount account) {
        String login = deriveLogin(account);
        return userRepository.findOneWithAuthoritiesByLogin(login).orElseGet(() -> createUser(login, account));
    }

    private User createUser(String login, InvestecAccount account) {
        String[] parts = account != null ? account.accountName().split(" ", 2) : new String[] { login, "" };
        String firstName = parts[0];
        String lastName = parts.length > 1 ? parts[1] : "";

        Authority userRole = authorityRepository.findById(AuthoritiesConstants.USER).orElseThrow();

        User user = new User();
        user.setLogin(login);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        // Placeholder email — never used (no email auth); must be unique
        user.setEmail(login + "@investec.holder");
        // Random password — never used (no password auth)
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setActivated(true);
        user.setLangKey("en");
        user.setCreatedBy("investec-oauth");
        user.setCreatedDate(Instant.now());
        user.setAuthorities(Set.of(userRole));
        return userRepository.save(user);
    }

    private String issueJwt(User user) {
        String authorities = AuthoritiesConstants.USER;
        Instant now = Instant.now();
        Instant validity = now.plus(86400, ChronoUnit.SECONDS); // 24 h

        JwsHeader jwsHeader = JwsHeader.with(SecurityUtils.JWT_ALGORITHM).build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuedAt(now)
            .expiresAt(validity)
            .subject(user.getLogin())
            .claim(SecurityUtils.AUTHORITIES_CLAIM, authorities)
            .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    private boolean isFixtureMode() {
        return !env.acceptsProfiles(Profiles.of("investec-live"));
    }

    private static String deriveLogin(InvestecAccount account) {
        if (account == null) return "holder";
        // "Thabo Mokoena" → "thabo.mokoena"
        String base = account.accountName().toLowerCase(Locale.ENGLISH).replaceAll("\\s+", ".").replaceAll("[^a-z0-9.]", "");
        if (base.isEmpty()) base = "holder";
        // Truncate to JHipster's 50-char login limit
        return base.length() > 50 ? base.substring(0, 50) : base;
    }

    private static String baseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();
        boolean defaultPort = ("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443);
        return defaultPort ? scheme + "://" + host : scheme + "://" + host + ":" + port;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
