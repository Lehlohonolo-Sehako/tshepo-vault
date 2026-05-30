package app.tshepo.web.rest;

import app.tshepo.domain.Credential;
import app.tshepo.security.SecurityUtils;
import app.tshepo.service.CredentialIssuanceService;
import app.tshepo.service.PresentationService;
import app.tshepo.web.rest.api.CredentialsApi;
import app.tshepo.web.rest.vm.*;
import java.util.List;
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
        String login = currentLogin();
        int p = page != null ? page : 0;
        int s = size != null ? size : 20;
        Page<Credential> credPage = issuanceService.listCredentials(login, p, s);

        List<CredentialResponse> items = credPage
            .getContent()
            .stream()
            .map(c -> CredentialIssuanceService.toCredentialResponse(c, null))
            .toList();

        CredentialListResponse response = new CredentialListResponse();
        response.setCredentials(items);
        response.setTotal((int) credPage.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CredentialResponse> getCredential(UUID id) {
        Credential cred = issuanceService.getCredential(currentLogin(), id);
        return ResponseEntity.ok(CredentialIssuanceService.toCredentialResponse(cred, null));
    }

    @Override
    public ResponseEntity<PresentationResponse> presentCredential(UUID id, PresentRequest request) {
        PresentationResponse response = presentationService.present(currentLogin(), id, request);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CredentialResponse> revokeCredential(UUID id) {
        Credential cred = issuanceService.revokeCredential(currentLogin(), id);
        return ResponseEntity.ok(CredentialIssuanceService.toCredentialResponse(cred, null));
    }

    private static String currentLogin() {
        return SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
