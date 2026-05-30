package app.tshepo.web.rest;

import app.tshepo.security.SecurityUtils;
import app.tshepo.service.BankConnectService;
import app.tshepo.web.rest.api.BankConnectionApi;
import app.tshepo.web.rest.vm.BankConnectRequest;
import app.tshepo.web.rest.vm.BankStatusResponse;
import app.tshepo.web.rest.vm.GetAvailableClaims200Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BankConnectionController implements BankConnectionApi {

    private final BankConnectService bankConnectService;

    public BankConnectionController(BankConnectService bankConnectService) {
        this.bankConnectService = bankConnectService;
    }

    @Override
    public ResponseEntity<BankStatusResponse> connectBank(BankConnectRequest request) {
        String login = currentLogin();
        String redirectUri = request.getRedirectUri() != null ? request.getRedirectUri().toString() : null;
        BankStatusResponse response = bankConnectService.connectBank(login, request.getAuthorizationCode(), redirectUri);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<BankStatusResponse> getBankStatus() {
        return ResponseEntity.ok(bankConnectService.getStatus(currentLogin()));
    }

    @Override
    public ResponseEntity<GetAvailableClaims200Response> getAvailableClaims() {
        return ResponseEntity.ok(bankConnectService.getAvailableClaims(currentLogin()));
    }

    private static String currentLogin() {
        return SecurityUtils.getCurrentUserLogin().orElseThrow(() ->
            new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED)
        );
    }
}
