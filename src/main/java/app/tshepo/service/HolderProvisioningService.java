package app.tshepo.service;

import app.tshepo.domain.Authority;
import app.tshepo.domain.User;
import app.tshepo.integration.investec.InvestecAccount;
import app.tshepo.integration.investec.InvestecTokenPair;
import app.tshepo.repository.AuthorityRepository;
import app.tshepo.repository.UserRepository;
import app.tshepo.security.AuthoritiesConstants;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provisions a holder user from an Investec account and connects the bank.
 * Runs in a single @Transactional so the user row is committed before the
 * controller redirects the browser (avoids a race on /api/account).
 */
@Service
public class HolderProvisioningService {

    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;
    private final BankConnectService bankConnectService;

    public HolderProvisioningService(
        UserRepository userRepository,
        AuthorityRepository authorityRepository,
        PasswordEncoder passwordEncoder,
        BankConnectService bankConnectService
    ) {
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.passwordEncoder = passwordEncoder;
        this.bankConnectService = bankConnectService;
    }

    @Transactional
    public User provisionAndConnect(InvestecAccount account, InvestecTokenPair tokens) {
        User user = findOrCreate(account);
        bankConnectService.connectBankWithTokens(user.getLogin(), tokens);
        return user;
    }

    private User findOrCreate(InvestecAccount account) {
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
        user.setEmail(login + "@investec.holder");
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setActivated(true);
        user.setLangKey("en");
        user.setCreatedBy("investec-oauth");
        user.setCreatedDate(Instant.now());
        user.setAuthorities(Set.of(userRole));
        return userRepository.save(user);
    }

    public static String deriveLogin(InvestecAccount account) {
        if (account == null) return "holder";
        String base = account.accountName().toLowerCase(Locale.ENGLISH).replaceAll("\\s+", ".").replaceAll("[^a-z0-9.]", "");
        if (base.isEmpty()) base = "holder";
        return base.length() > 50 ? base.substring(0, 50) : base;
    }
}
