package app.tshepo.web.rest;

import app.tshepo.security.SecurityUtils;
import app.tshepo.service.ApiKeyManagementService;
import app.tshepo.web.rest.api.ApiKeyManagementApi;
import app.tshepo.web.rest.vm.CreateApiKey201Response;
import app.tshepo.web.rest.vm.CreateApiKeyRequest;
import app.tshepo.web.rest.vm.ListApiKeys200Response;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ApiKeyController implements ApiKeyManagementApi {

    private final ApiKeyManagementService apiKeyService;

    public ApiKeyController(ApiKeyManagementService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    public ResponseEntity<CreateApiKey201Response> createApiKey(CreateApiKeyRequest request) {
        CreateApiKey201Response response = apiKeyService.createKey(currentLogin(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<ListApiKeys200Response> listApiKeys() {
        return ResponseEntity.ok(apiKeyService.listKeys(currentLogin()));
    }

    @Override
    public ResponseEntity<Void> revokeApiKey(UUID id) {
        apiKeyService.revokeKey(currentLogin(), id);
        return ResponseEntity.noContent().build();
    }

    private static String currentLogin() {
        return SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
