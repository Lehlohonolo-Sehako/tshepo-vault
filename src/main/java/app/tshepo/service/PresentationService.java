package app.tshepo.service;

import app.tshepo.domain.Credential;
import app.tshepo.domain.VerificationEvent;
import app.tshepo.domain.enumeration.CredentialStatus;
import app.tshepo.domain.enumeration.VerificationResult;
import app.tshepo.repository.CredentialRepository;
import app.tshepo.repository.VerificationEventRepository;
import app.tshepo.web.rest.vm.ClaimType;
import app.tshepo.web.rest.vm.PresentRequest;
import app.tshepo.web.rest.vm.PresentationResponse;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class PresentationService {

    private final CredentialRepository credentialRepository;
    private final VerificationEventRepository verificationEventRepository;
    private final SdJwtService sdJwtService;

    public PresentationService(
        CredentialRepository credentialRepository,
        VerificationEventRepository verificationEventRepository,
        SdJwtService sdJwtService
    ) {
        this.credentialRepository = credentialRepository;
        this.verificationEventRepository = verificationEventRepository;
        this.sdJwtService = sdJwtService;
    }

    public PresentationResponse present(String holderLogin, UUID credentialUuid, PresentRequest request) {
        Credential cred = credentialRepository
            .findByCredentialUuidAndHolderLogin(credentialUuid, holderLogin)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Credential not found"));

        if (cred.getStatus() == CredentialStatus.REVOKED) {
            throw new ResponseStatusException(HttpStatus.GONE, "Credential is revoked");
        }
        if (cred.getStatus() == CredentialStatus.EXPIRED || (cred.getExpiresAt() != null && cred.getExpiresAt().isBefore(Instant.now()))) {
            throw new ResponseStatusException(HttpStatus.GONE, "Credential is expired");
        }

        // Validate requested claim types exist in this credential
        Set<String> available = cred
            .getClaimses()
            .stream()
            .map(ic -> ic.getClaimType().name().toLowerCase())
            .collect(Collectors.toSet());

        List<ClaimType> requestedTypes = request.getDisclosedClaimTypes();
        if (requestedTypes == null || requestedTypes.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one claim must be selected");
        }

        Set<String> requested = requestedTypes
            .stream()
            .map(t -> t.getValue().toLowerCase())
            .collect(Collectors.toSet());

        Set<String> missing = requested
            .stream()
            .filter(r -> !available.contains(r))
            .collect(Collectors.toSet());

        if (!missing.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Claim types not present in this credential: " + missing);
        }

        // Build selective-disclosure presentation
        String presentationToken = sdJwtService.createPresentation(cred.getSdJwt(), requested);

        // Audit record (holder-initiated; apiKey = null)
        VerificationEvent event = new VerificationEvent();
        event.setVerifiedAt(Instant.now());
        event.setResult(VerificationResult.VALID);
        event.setDisclosedClaims(requested.toString());
        event.setCredentialRef("urn:tshepo:" + credentialUuid);
        verificationEventRepository.save(event);

        PresentationResponse response = new PresentationResponse();
        response.setToken(presentationToken);
        response.setCredentialId(credentialUuid);
        response.setDisclosedClaimTypes(requestedTypes);
        if (cred.getExpiresAt() != null) {
            response.setExpiresAt(OffsetDateTime.ofInstant(cred.getExpiresAt(), ZoneOffset.UTC));
        }
        return response;
    }
}
