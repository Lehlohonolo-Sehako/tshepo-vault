package app.tshepo.service;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Manages the issuer's EC P-256 signing key.
 * In dev the key is generated fresh at startup. In production, load from a key store.
 */
@Service
public class IssuerKeyService {

    private static final Logger LOG = LoggerFactory.getLogger(IssuerKeyService.class);
    private static final String KEY_ID = "tshepo-key-1";

    private final TshepoProperties props;
    private ECKey keyPair;

    public IssuerKeyService(TshepoProperties props) {
        this.props = props;
    }

    @PostConstruct
    void generateKey() {
        try {
            keyPair = new ECKeyGenerator(Curve.P_256).keyUse(KeyUse.SIGNATURE).keyID(KEY_ID).algorithm(JWSAlgorithm.ES256).generate();
            LOG.info("Issuer key generated: kid={}, did={}", KEY_ID, props.getIssuerDid());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate issuer EC key", e);
        }
    }

    public ECDSASigner signer() {
        try {
            return new ECDSASigner(keyPair);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create signer", e);
        }
    }

    public ECDSAVerifier verifier() {
        try {
            return new ECDSAVerifier(keyPair.toPublicJWK());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create verifier", e);
        }
    }

    public String keyId() {
        return KEY_ID;
    }

    public String issuerDid() {
        return props.getIssuerDid();
    }

    /** Public JWK as a JSON-serialisable map, safe to serve over HTTP. */
    public Map<String, Object> publicJwkMap() {
        return keyPair.toPublicJWK().toJSONObject();
    }
}
