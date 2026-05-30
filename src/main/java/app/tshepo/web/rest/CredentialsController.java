package app.tshepo.web.rest;

import app.tshepo.security.SecurityUtils;
import app.tshepo.service.CredentialIssuanceService;
import app.tshepo.service.PresentationService;
import app.tshepo.web.rest.api.CredentialsApi;
import app.tshepo.web.rest.vm.*;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class CredentialsController implements CredentialsApi {

    private final CredentialIssuanceService issuanceService;
    private final PresentationService presentationService;

    public CredentialsController(CredentialIssuanceService issuanceService, PresentationService presentationService) {
        this.issuanceService = issuanceService;
        this.presentationService = presentationService;
    }

    @Override
    public ResponseEntity<CredentialResponse> issueCredential(CredentialIssueRequest request) {
        CredentialResponse response = issuanceService.issueCredential(currentLogin(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<CredentialListResponse> listCredentials(String status, Integer page, Integer size) {
        Page<CredentialResponse> credPage = issuanceService.listCredentials(
            currentLogin(),
            page != null ? page : 0,
            size != null ? size : 20
        );
        CredentialListResponse response = new CredentialListResponse();
        response.setCredentials(credPage.getContent());
        response.setTotal((int) credPage.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CredentialResponse> getCredential(UUID id) {
        return ResponseEntity.ok(issuanceService.getCredential(currentLogin(), id));
    }

    @Override
    public ResponseEntity<PresentationResponse> presentCredential(UUID id, PresentRequest request) {
        PresentationResponse response = presentationService.present(currentLogin(), id, request);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CredentialResponse> revokeCredential(UUID id) {
        return ResponseEntity.ok(issuanceService.revokeCredential(currentLogin(), id));
    }

    private static String currentLogin() {
        return SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
