package app.tshepo.service;

import app.tshepo.domain.VerifierApiKey;
import app.tshepo.repository.VerifierApiKeyRepository;
import app.tshepo.web.rest.vm.CreateApiKey201Response;
import app.tshepo.web.rest.vm.CreateApiKeyRequest;
import app.tshepo.web.rest.vm.ListApiKeys200Response;
import app.tshepo.web.rest.vm.VerifierApiKeyResponse;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ApiKeyManagementService {

    private static final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder(10);

    private final VerifierApiKeyRepository verifierApiKeyRepository;

    public ApiKeyManagementService(VerifierApiKeyRepository verifierApiKeyRepository) {
        this.verifierApiKeyRepository = verifierApiKeyRepository;
    }

    public CreateApiKey201Response createKey(String ownerLogin, CreateApiKeyRequest request) {
        String rawKey = "tshpo_" + UUID.randomUUID().toString().replace("-", "");
        String keyHash = BCRYPT.encode(rawKey);

        VerifierApiKey entity = new VerifierApiKey();
        entity.setKeyUuid(UUID.randomUUID());
        entity.setOwnerLogin(ownerLogin);
        entity.setLabel(request.getLabel());
        entity.setKeyHash(keyHash);
        entity.setActive(true);
        entity.setCallCount(0L);
        entity.setCreatedAt(Instant.now());
        verifierApiKeyRepository.save(entity);

        CreateApiKey201Response response = new CreateApiKey201Response();
        response.setId(entity.getKeyUuid());
        response.setLabel(entity.getLabel());
        response.setActive(entity.getActive());
        response.setCallCount(0);
        response.setCreatedAt(OffsetDateTime.ofInstant(entity.getCreatedAt(), ZoneOffset.UTC));
        response.setRawKey(rawKey);
        return response;
    }

    @Transactional(readOnly = true)
    public ListApiKeys200Response listKeys(String ownerLogin) {
        List<VerifierApiKeyResponse> keys = verifierApiKeyRepository
            .findAllByOwnerLoginAndActiveTrueOrderByCreatedAtDesc(ownerLogin)
            .stream()
            .map(ApiKeyManagementService::toApiKeyResponse)
            .toList();

        ListApiKeys200Response response = new ListApiKeys200Response();
        response.setKeys(keys);
        return response;
    }

    public void revokeKey(String ownerLogin, UUID keyUuid) {
        VerifierApiKey key = verifierApiKeyRepository
            .findByKeyUuidAndOwnerLogin(keyUuid, ownerLogin)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "API key not found"));
        key.setActive(false);
        verifierApiKeyRepository.save(key);
    }

    /**
     * Validates a raw API key against stored bcrypt hashes.
     * Returns the matching entity if valid and active.
     */
    @Transactional(readOnly = true)
    public Optional<VerifierApiKey> validateKey(String rawKey) {
        if (rawKey == null || rawKey.isBlank()) return Optional.empty();
        return verifierApiKeyRepository
            .findAll()
            .stream()
            .filter(k -> k.getActive() != null && k.getActive())
            .filter(k -> BCRYPT.matches(rawKey, k.getKeyHash()))
            .findFirst();
    }

    private static VerifierApiKeyResponse toApiKeyResponse(VerifierApiKey key) {
        VerifierApiKeyResponse r = new VerifierApiKeyResponse();
        r.setId(key.getKeyUuid());
        r.setLabel(key.getLabel());
        r.setActive(key.getActive());
        r.setCallCount(key.getCallCount() != null ? key.getCallCount().intValue() : 0);
        if (key.getCreatedAt() != null) {
            r.setCreatedAt(OffsetDateTime.ofInstant(key.getCreatedAt(), ZoneOffset.UTC));
        }
        return r;
    }
}
