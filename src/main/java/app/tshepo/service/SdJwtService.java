package app.tshepo.service;

import app.tshepo.domain.IssuedClaim;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;

/**
 * Implements SD-JWT (Selective Disclosure JWT) per the IETF SD-JWT spec.
 *
 * Serialised form: <issuer-JWT>~<disclosure1>~<disclosure2>~
 * - Disclosures: BASE64URL(["<salt>","<claim_name>",<claim_value>])
 * - JWT payload contains _sd: [SHA-256 hash of each disclosure, base64url-encoded]
 *
 * Privacy: disclosures for non-selected claims are stripped at presentation time.
 * The verifier receives only the disclosures the holder chose to reveal.
 */
@Service
public class SdJwtService {

    private final IssuerKeyService issuerKeyService;
    private final ObjectMapper objectMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    public SdJwtService(IssuerKeyService issuerKeyService, ObjectMapper objectMapper) {
        this.issuerKeyService = issuerKeyService;
        this.objectMapper = objectMapper;
    }

    /**
     * Builds a full SD-JWT (all disclosures included) for the given claims.
     * The returned string is stored in Credential.sdJwt — it is never sent directly to clients.
     */
    public String buildSdJwt(List<IssuedClaim> claims, String holderDid, Instant issuedAt, Instant expiresAt, String jti) {
        try {
            List<String> disclosures = new ArrayList<>();
            List<String> sdHashes = new ArrayList<>();

            for (IssuedClaim claim : claims) {
                String claimName = claim.getClaimType().name().toLowerCase();
                Map<String, Object> claimValue = new LinkedHashMap<>();
                claimValue.put("met", claim.getMet());
                claimValue.put("operator", claim.getOperator().name());
                claimValue.put("threshold", claim.getThreshold());
                if (claim.getCurrency() != null) claimValue.put("currency", claim.getCurrency());
                if (claim.getPeriodMonths() != null) claimValue.put("periodMonths", claim.getPeriodMonths());

                String disclosure = buildDisclosure(claimName, claimValue);
                disclosures.add(disclosure);
                sdHashes.add(hashDisclosure(disclosure));
            }

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(issuerKeyService.issuerDid())
                .subject(holderDid)
                .issueTime(Date.from(issuedAt))
                .expirationTime(Date.from(expiresAt))
                .jwtID(jti)
                .claim("_sd_alg", "sha-256")
                .claim("_sd", sdHashes)
                .claim(
                    "vc",
                    Map.of(
                        "@context",
                        List.of("https://www.w3.org/2018/credentials/v1"),
                        "type",
                        List.of("VerifiableCredential", "ProofOfFundsCredential")
                    )
                )
                .build();

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(issuerKeyService.keyId()).build();

            SignedJWT jwt = new SignedJWT(header, claimsSet);
            jwt.sign(issuerKeyService.signer());

            return assemble(jwt.serialize(), disclosures);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build SD-JWT", e);
        }
    }

    /**
     * Creates a presentation by retaining only the disclosures for the requested claim names.
     * All other disclosures are stripped — the verifier cannot determine they exist.
     */
    public String createPresentation(String storedSdJwt, Set<String> claimNamesToDisclose) {
        String[] parts = storedSdJwt.split("~", -1);
        String jwt = parts[0];
        List<String> filteredDisclosures = new ArrayList<>();

        for (int i = 1; i < parts.length; i++) {
            String disclosure = parts[i];
            if (disclosure.isEmpty()) continue;
            String claimName = extractClaimName(disclosure);
            if (claimName != null && claimNamesToDisclose.contains(claimName)) {
                filteredDisclosures.add(disclosure);
            }
        }

        return assemble(jwt, filteredDisclosures);
    }

    /**
     * Verifies a presentation token. Returns null on parse failure (MALFORMED).
     */
    public ParsedPresentation parseAndVerifyPresentation(String token) {
        try {
            String[] parts = token.split("~", -1);
            if (parts.length == 0 || parts[0].isBlank()) return null;

            SignedJWT jwt = SignedJWT.parse(parts[0]);

            // Verify signature
            if (!jwt.verify(issuerKeyService.verifier())) return null;

            JWTClaimsSet claims = jwt.getJWTClaimsSet();

            // Parse disclosures
            List<Map<String, Object>> disclosedClaims = new ArrayList<>();
            List<String> expectedHashes = (List<String>) claims.getClaim("_sd");

            for (int i = 1; i < parts.length; i++) {
                String disclosure = parts[i];
                if (disclosure.isEmpty()) continue;

                // Verify disclosure hash is in _sd
                String hash = hashDisclosure(disclosure);
                if (expectedHashes == null || !expectedHashes.contains(hash)) continue;

                String claimName = extractClaimName(disclosure);
                Object claimValue = extractClaimValue(disclosure);
                if (claimName != null && claimValue != null) {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("name", claimName);
                    entry.put("value", claimValue);
                    disclosedClaims.add(entry);
                }
            }

            return new ParsedPresentation(
                claims.getIssuer(),
                claims.getSubject(),
                claims.getJWTID(),
                claims.getExpirationTime() != null ? claims.getExpirationTime().toInstant() : null,
                disclosedClaims
            );
        } catch (Exception e) {
            return null;
        }
    }

    // --- internal helpers ---

    private String buildDisclosure(String claimName, Object claimValue) {
        try {
            byte[] saltBytes = new byte[16];
            secureRandom.nextBytes(saltBytes);
            String salt = Base64.getUrlEncoder().withoutPadding().encodeToString(saltBytes);
            List<Object> payload = List.of(salt, claimName, claimValue);
            String json = objectMapper.writeValueAsString(payload);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build disclosure", e);
        }
    }

    private String hashDisclosure(String disclosure) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha256.digest(disclosure.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private String extractClaimName(String disclosure) {
        try {
            String json = new String(Base64.getUrlDecoder().decode(disclosure), StandardCharsets.UTF_8);
            List<?> parts = objectMapper.readValue(json, List.class);
            return parts.size() >= 2 ? (String) parts.get(1) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Object extractClaimValue(String disclosure) {
        try {
            String json = new String(Base64.getUrlDecoder().decode(disclosure), StandardCharsets.UTF_8);
            List<?> parts = objectMapper.readValue(json, List.class);
            return parts.size() >= 3 ? parts.get(2) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static String assemble(String jwt, List<String> disclosures) {
        StringBuilder sb = new StringBuilder(jwt);
        for (String d : disclosures) {
            sb.append('~').append(d);
        }
        sb.append('~'); // trailing ~ per spec (no key binding JWT)
        return sb.toString();
    }

    public record ParsedPresentation(
        String issuer,
        String subject,
        String jti,
        Instant expiresAt,
        List<Map<String, Object>> disclosedClaims
    ) {}
}
