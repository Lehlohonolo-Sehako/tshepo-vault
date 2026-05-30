package app.tshepo.web.rest;

import app.tshepo.service.ApiKeyManagementService;
import app.tshepo.service.VerificationService;
import app.tshepo.web.rest.api.VerificationApi;
import app.tshepo.web.rest.vm.VerifyRequest;
import app.tshepo.web.rest.vm.VerifyResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VerificationController implements VerificationApi {

    private final VerificationService verificationService;
    private final ApiKeyManagementService apiKeyManagementService;
    private final HttpServletRequest httpRequest;

    public VerificationController(
        VerificationService verificationService,
        ApiKeyManagementService apiKeyManagementService,
        HttpServletRequest httpRequest
    ) {
        this.verificationService = verificationService;
        this.apiKeyManagementService = apiKeyManagementService;
        this.httpRequest = httpRequest;
    }

    /** Public endpoint — no JWT required; rate-limited at infra level. */
    @Override
    public ResponseEntity<VerifyResponse> verifyPresentation(VerifyRequest request) {
        return ResponseEntity.ok(verificationService.verify(request, null));
    }

    /** Metered endpoint — requires X-API-Key header. */
    @Override
    public ResponseEntity<VerifyResponse> verifyPresentationMetered(VerifyRequest request) {
        String rawKey = httpRequest.getHeader("X-API-Key");
        Long apiKeyId = apiKeyManagementService
            .validateKey(rawKey)
            .map(k -> k.getId())
            .orElse(null);

        if (apiKeyId == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(verificationService.verify(request, apiKeyId));
    }
}
