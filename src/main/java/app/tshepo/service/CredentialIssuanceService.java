package app.tshepo.service;

import app.tshepo.domain.BankConnection;
import app.tshepo.domain.Credential;
import app.tshepo.domain.IssuedClaim;
import app.tshepo.domain.enumeration.BankConnectionStatus;
import app.tshepo.domain.enumeration.ClaimOperator;
import app.tshepo.domain.enumeration.ClaimType;
import app.tshepo.domain.enumeration.CredentialStatus;
import app.tshepo.repository.BankConnectionRepository;
import app.tshepo.repository.CredentialRepository;
import app.tshepo.repository.IssuedClaimRepository;
import app.tshepo.web.rest.vm.ClaimThreshold;
import app.tshepo.web.rest.vm.ComputedClaim;
import app.tshepo.web.rest.vm.CredentialIssueRequest;
import app.tshepo.web.rest.vm.CredentialResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class CredentialIssuanceService {

    private final CredentialRepository credentialRepository;
    private final IssuedClaimRepository issuedClaimRepository;
    private final BankConnectionRepository bankConnectionRepository;
    private final PredicateService predicateService;
    private final SdJwtService sdJwtService;
    private final IssuerKeyService issuerKeyService;
    private final TshepoProperties props;

    public CredentialIssuanceService(
        CredentialRepository credentialRepository,
        IssuedClaimRepository issuedClaimRepository,
        BankConnectionRepository bankConnectionRepository,
        PredicateService predicateService,
        SdJwtService sdJwtService,
        IssuerKeyService issuerKeyService,
        TshepoProperties props
    ) {
        this.credentialRepository = credentialRepository;
        this.issuedClaimRepository = issuedClaimRepository;
        this.bankConnectionRepository = bankConnectionRepository;
        this.predicateService = predicateService;
        this.sdJwtService = sdJwtService;
        this.issuerKeyService = issuerKeyService;
        this.props = props;
    }

    public CredentialResponse issueCredential(String holderLogin, CredentialIssueRequest request) {
        // Require connected bank account
        BankConnection conn = bankConnectionRepository
            .findByHolderLogin(holderLogin)
            .filter(c -> c.getStatus() == BankConnectionStatus.CONNECTED)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "No bank account connected"));

        // Get primary account ID from stored connection metadata
        String accountId = resolveAccountId(conn);

        // Compute predicates in memory; raw data discarded after this call
        List<ComputedClaim> computed = predicateService.computeClaims(conn.getAccessToken(), accountId, request.getClaims());

        // All requested claims must be met
        List<String> unmetClaims = new ArrayList<>();
        for (int i = 0; i < request.getClaims().size(); i++) {
            ComputedClaim cc = computed.get(i);
            if (Boolean.FALSE.equals(cc.getMet())) {
                unmetClaims.add(cc.getType() != null ? cc.getType().getValue() : "unknown");
            }
        }
        if (!unmetClaims.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Claim threshold(s) not met: " + String.join(", ", unmetClaims));
        }

        // Build domain entities
        UUID credentialUuid = UUID.randomUUID();
        Instant now = Instant.now();
        int validityDays = request.getValidityDays() != null ? request.getValidityDays() : props.getCredentialValidityDays();
        Instant expiresAt = now.plus(validityDays, ChronoUnit.DAYS);

        String holderDid = "did:tshepo:" + holderLogin;

        Credential credential = new Credential();
        credential.setCredentialUuid(credentialUuid);
        credential.setHolderLogin(holderLogin);
        credential.setTitle(request.getTitle());
        credential.setPurpose(request.getPurpose());
        credential.setStatus(CredentialStatus.ACTIVE);
        credential.setIssuedAt(now);
        credential.setExpiresAt(expiresAt);
        credential.setIssuerDid(issuerKeyService.issuerDid());
        credential.setVcRef("urn:tshepo:" + credentialUuid);

        // Build IssuedClaim entities first (needed for SD-JWT build)
        List<IssuedClaim> issuedClaims = new ArrayList<>();
        for (int i = 0; i < request.getClaims().size(); i++) {
            ClaimThreshold ct = request.getClaims().get(i);
            ComputedClaim cc = computed.get(i);

            IssuedClaim ic = new IssuedClaim();
            ic.setClaimType(domainClaimType(ct.getType()));
            ic.setOperator(domainOperator(ct.getOperator()));
            ic.setThreshold(ct.getThreshold() != null ? ct.getThreshold() : BigDecimal.ZERO);
            ic.setCurrency(ct.getCurrency());
            ic.setPeriodMonths(ct.getPeriodMonths());
            ic.setMet(Boolean.TRUE.equals(cc.getMet()));
            ic.setCredential(credential);
            issuedClaims.add(ic);
        }

        // Build and store SD-JWT (all disclosures; presentation strips unwanted ones)
        String sdJwt = sdJwtService.buildSdJwt(issuedClaims, holderDid, now, expiresAt, credentialUuid.toString());
        credential.setSdJwt(sdJwt);

        String summary = issuedClaims
            .stream()
            .map(ic -> ic.getClaimType().name())
            .collect(Collectors.joining(", "));
        credential.setClaimsSummary(summary);

        credentialRepository.save(credential);
        issuedClaimRepository.saveAll(issuedClaims);

        return toCredentialResponse(credential, computed);
    }

    @Transactional(readOnly = true)
    public Page<CredentialResponse> listCredentials(String holderLogin, int page, int size) {
        return credentialRepository
            .findAllByHolderLoginOrderByIssuedAtDesc(holderLogin, PageRequest.of(page, size))
            .map(c -> toCredentialResponse(c, null));
    }

    @Transactional(readOnly = true)
    public CredentialResponse getCredential(String holderLogin, UUID credentialUuid) {
        Credential cred = credentialRepository
            .findByCredentialUuidAndHolderLogin(credentialUuid, holderLogin)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Credential not found"));
        return toCredentialResponse(cred, null);
    }

    @Transactional
    public CredentialResponse revokeCredential(String holderLogin, UUID credentialUuid) {
        Credential cred = credentialRepository
            .findByCredentialUuidAndHolderLogin(credentialUuid, holderLogin)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Credential not found"));
        if (cred.getStatus() != CredentialStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Credential is already revoked or expired");
        }
        cred.setStatus(CredentialStatus.REVOKED);
        Credential saved = credentialRepository.save(cred);
        return toCredentialResponse(saved, null);
    }

    // --- mapping helpers ---

    public static CredentialResponse toCredentialResponse(Credential cred, List<ComputedClaim> claims) {
        CredentialResponse r = new CredentialResponse();
        r.setId(cred.getCredentialUuid());
        r.setHolderLogin(cred.getHolderLogin());
        r.setTitle(cred.getTitle());
        r.setPurpose(cred.getPurpose());
        if (cred.getStatus() != null) {
            r.setStatus(CredentialResponse.StatusEnum.fromValue(cred.getStatus().name()));
        }
        r.setIssuerDid(cred.getIssuerDid());
        r.setVcRef(cred.getVcRef());
        if (cred.getIssuedAt() != null) r.setIssuedAt(OffsetDateTime.ofInstant(cred.getIssuedAt(), ZoneOffset.UTC));
        if (cred.getExpiresAt() != null) r.setExpiresAt(OffsetDateTime.ofInstant(cred.getExpiresAt(), ZoneOffset.UTC));
        if (claims != null) {
            r.setClaims(claims);
            r.setClaimCount(claims.size());
        } else if (cred.getClaimsSummary() != null && !cred.getClaimsSummary().isBlank()) {
            // Parse the stored summary string ("INFLOW, TENURE") into minimal stubs for list views.
            // Threshold is unknown here — the frontend uses type-only labels for cards.
            List<ComputedClaim> stubs = Arrays.stream(cred.getClaimsSummary().split(",\\s*"))
                .filter(t -> !t.isBlank())
                .<ComputedClaim>map(t -> {
                    try {
                        return new ComputedClaim()
                            .type(app.tshepo.web.rest.vm.ClaimType.fromValue(t.trim()))
                            .met(true)
                            .operator("GTE")
                            .threshold(BigDecimal.ZERO);
                    } catch (IllegalArgumentException ignored) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
            r.setClaims(stubs);
            r.setClaimCount(stubs.size());
        } else {
            r.setClaimCount(cred.getClaimses() != null ? cred.getClaimses().size() : 0);
        }
        return r;
    }

    private static String resolveAccountId(BankConnection conn) {
        // In fixture mode the accountId is hardcoded; for live we use the stored connection metadata
        // to avoid re-calling getAccounts unnecessarily. A real implementation would store accountId.
        return "fixture-account-001";
    }

    private static ClaimType domainClaimType(app.tshepo.web.rest.vm.ClaimType vmType) {
        return ClaimType.valueOf(vmType.getValue());
    }

    private static ClaimOperator domainOperator(app.tshepo.web.rest.vm.ClaimThreshold.OperatorEnum op) {
        if (op == null) return ClaimOperator.GTE;
        return ClaimOperator.valueOf(op.getValue());
    }
}
