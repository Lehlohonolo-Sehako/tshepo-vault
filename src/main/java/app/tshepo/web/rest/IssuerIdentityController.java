package app.tshepo.web.rest;

import app.tshepo.service.IssuerKeyService;
import app.tshepo.web.rest.api.IssuerIdentityApi;
import app.tshepo.web.rest.vm.DidDocument;
import app.tshepo.web.rest.vm.JwksResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IssuerIdentityController implements IssuerIdentityApi {

    private final IssuerKeyService issuerKeyService;

    public IssuerIdentityController(IssuerKeyService issuerKeyService) {
        this.issuerKeyService = issuerKeyService;
    }

    @Override
    public ResponseEntity<JwksResponse> getJwks() {
        JwksResponse response = new JwksResponse();
        response.setKeys(List.of(issuerKeyService.publicJwkMap()));
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<DidDocument> getDidDocument() {
        String did = issuerKeyService.issuerDid();
        String keyId = issuerKeyService.keyId();

        Map<String, Object> verificationMethod = new LinkedHashMap<>();
        verificationMethod.put("id", did + "#" + keyId);
        verificationMethod.put("type", "JsonWebKey2020");
        verificationMethod.put("controller", did);
        verificationMethod.put("publicKeyJwk", issuerKeyService.publicJwkMap());

        DidDocument doc = new DidDocument();
        doc.setAtContext(List.of("https://www.w3.org/ns/did/v1", "https://w3id.org/security/suites/jws-2020/v1"));
        doc.setId(did);
        doc.setVerificationMethod(List.of(verificationMethod));
        doc.setAssertionMethod(List.of(did + "#" + keyId));
        return ResponseEntity.ok(doc);
    }
}
