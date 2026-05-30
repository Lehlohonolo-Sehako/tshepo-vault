package app.tshepo.service;

import app.tshepo.domain.Credential;
import app.tshepo.domain.VerificationEvent;
import app.tshepo.domain.VerifierApiKey;
import app.tshepo.domain.enumeration.CredentialStatus;
import app.tshepo.domain.enumeration.VerificationResult;
import app.tshepo.repository.CredentialRepository;
import app.tshepo.repository.VerificationEventRepository;
import app.tshepo.repository.VerifierApiKeyRepository;
import app.tshepo.web.rest.vm.ClaimType;
import app.tshepo.web.rest.vm.DisclosedClaim;
import app.tshepo.web.rest.vm.VerifyRequest;
import app.tshepo.web.rest.vm.VerifyResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VerificationService {

    private final SdJwtService sdJwtService;
    private final IssuerKeyService issuerKeyService;
    private final CredentialRepository credentialRepository;
    private final VerificationEventRepository verificationEventRepository;
    private final VerifierApiKeyRepository verifierApiKeyRepository;

    public VerificationService(
        SdJwtService sdJwtService,
        IssuerKeyService issuerKeyService,
        CredentialRepository credentialRepository,
        VerificationEventRepository verificationEventRepository,
        VerifierApiKeyRepository verifierApiKeyRepository
    ) {
        this.sdJwtService = sdJwtService;
        this.issuerKeyService = issuerKeyService;
        this.credentialRepository = credentialRepository;
        this.verificationEventRepository = verificationEventRepository;
        this.verifierApiKeyRepository = verifierApiKeyRepository;
    }

    public VerifyResponse verify(VerifyRequest request, Long apiKeyId) {
        if (request.getToken() == null || request.getToken().isBlank()) {
            return invalid(VerifyResponse.ErrorEnum.MALFORMED, null, apiKeyId);
        }

        // Parse + verify signature
        SdJwtService.ParsedPresentation parsed = sdJwtService.parseAndVerifyPresentation(request.getToken());
        if (parsed == null) {
            return invalid(VerifyResponse.ErrorEnum.MALFORMED, null, apiKeyId);
        }

        // Signature is valid — now check issuer trust
        if (!issuerKeyService.issuerDid().equals(parsed.issuer())) {
            return invalid(VerifyResponse.ErrorEnum.UNTRUSTED_ISSUER, parsed, apiKeyId);
        }

        // Expiry check
        if (parsed.expiresAt() != null && parsed.expiresAt().isBefore(Instant.now())) {
            return invalid(VerifyResponse.ErrorEnum.EXPIRED, parsed, apiKeyId);
        }

        // Revocation check via jti → credentialUuid
        if (parsed.jti() != null) {
            try {
                UUID credUuid = UUID.fromString(parsed.jti());
                Optional<Credential> opt = credentialRepository.findByCredentialUuid(credUuid);
                if (opt.isPresent() && opt.get().getStatus() == CredentialStatus.REVOKED) {
                    return invalid(VerifyResponse.ErrorEnum.REVOKED, parsed, apiKeyId);
                }
            } catch (IllegalArgumentException ignored) {
                // jti is not a UUID — treat as unknown credential, proceed
            }
        }

        // Build response
        List<DisclosedClaim> disclosedClaims = mapDisclosedClaims(parsed.disclosedClaims());

        recordEvent(
            VerificationResult.VALID,
            parsed.jti(),
            disclosedClaims
                .stream()
                .map(d -> d.getType() != null ? d.getType().getValue() : "?")
                .toList()
                .toString(),
            apiKeyId
        );

        if (apiKeyId != null) {
            verifierApiKeyRepository.incrementCallCount(apiKeyId);
        }

        VerifyResponse response = new VerifyResponse();
        response.setValid(true);
        response.setIssuerDid(parsed.issuer());
        response.setHolderDid(parsed.subject());
        response.setDisclosedClaims(disclosedClaims);
        if (parsed.expiresAt() != null) {
            response.setExpiresAt(OffsetDateTime.ofInstant(parsed.expiresAt(), ZoneOffset.UTC));
        }
        return response;
    }

    private VerifyResponse invalid(VerifyResponse.ErrorEnum error, SdJwtService.ParsedPresentation parsed, Long apiKeyId) {
        recordEvent(toVerificationResult(error), parsed != null ? parsed.jti() : null, null, apiKeyId);
        VerifyResponse r = new VerifyResponse();
        r.setValid(false);
        r.setError(error);
        if (parsed != null) r.setIssuerDid(parsed.issuer());
        return r;
    }

    private void recordEvent(VerificationResult result, String credentialRef, String disclosedClaims, Long apiKeyId) {
        VerificationEvent event = new VerificationEvent();
        event.setVerifiedAt(Instant.now());
        event.setResult(result);
        if (credentialRef != null) event.setCredentialRef("urn:tshepo:" + credentialRef);
        event.setDisclosedClaims(disclosedClaims);
        if (apiKeyId != null) {
            verifierApiKeyRepository.findById(apiKeyId).ifPresent(event::setApiKey);
        }
        verificationEventRepository.save(event);
    }

    private List<DisclosedClaim> mapDisclosedClaims(List<Map<String, Object>> raw) {
        List<DisclosedClaim> result = new ArrayList<>();
        for (Map<String, Object> entry : raw) {
            DisclosedClaim dc = new DisclosedClaim();
            String claimName = (String) entry.get("name");
            if (claimName != null) {
                try {
                    dc.setType(ClaimType.fromValue(claimName.toUpperCase()));
                } catch (Exception ignored) {}
            }
            Object value = entry.get("value");
            if (value instanceof Map<?, ?> valueMap) {
                Object met = valueMap.get("met");
                if (met instanceof Boolean b) dc.setMet(b);
                Object op = valueMap.get("operator");
                if (op instanceof String s) dc.setOperator(s);
                Object thr = valueMap.get("threshold");
                if (thr instanceof Number n) dc.setThreshold(new BigDecimal(n.toString()));
                Object cur = valueMap.get("currency");
                if (cur instanceof String s) dc.setCurrency(s);
            }
            result.add(dc);
        }
        return result;
    }

    private static VerificationResult toVerificationResult(VerifyResponse.ErrorEnum error) {
        if (error == null) return VerificationResult.VALID;
        return switch (error) {
            case EXPIRED -> VerificationResult.EXPIRED;
            case REVOKED -> VerificationResult.REVOKED;
            case SIGNATURE_INVALID -> VerificationResult.INVALID_SIGNATURE;
            case UNTRUSTED_ISSUER -> VerificationResult.UNTRUSTED_ISSUER;
            default -> VerificationResult.MALFORMED;
        };
    }
}
