package app.tshepo.service;

import static org.assertj.core.api.Assertions.assertThat;

import app.tshepo.domain.IssuedClaim;
import app.tshepo.domain.enumeration.ClaimOperator;
import app.tshepo.domain.enumeration.ClaimType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SdJwtServiceTest {

    private SdJwtService sdJwtService;

    @BeforeEach
    void setUp() {
        TshepoProperties props = new TshepoProperties();
        props.setIssuerDid("did:web:localhost");
        IssuerKeyService keyService = new IssuerKeyService(props);
        keyService.generateKey();
        sdJwtService = new SdJwtService(keyService, new ObjectMapper());
    }

    @Test
    void buildSdJwt_returnsTokenWithExpectedStructure() {
        List<IssuedClaim> claims = List.of(inflowClaim(30_000, true), balanceClaim(50_000, true));
        String token = sdJwtService.buildSdJwt(claims, "did:tshepo:alice", now(), later(), "jti-1");

        assertThat(token).isNotBlank();
        // issuer JWT ~ disclosure ~ disclosure ~ (trailing ~)
        String[] parts = token.split("~", -1);
        assertThat(parts.length).isGreaterThanOrEqualTo(3); // jwt + 2 disclosures + empty trailing
        assertThat(parts[parts.length - 1]).isEmpty(); // trailing ~
    }

    @Test
    void createPresentation_stripsNonDisclosedClaims() {
        List<IssuedClaim> claims = List.of(inflowClaim(30_000, true), balanceClaim(50_000, true));
        String fullToken = sdJwtService.buildSdJwt(claims, "did:tshepo:alice", now(), later(), "jti-2");

        // Disclose only INFLOW
        String presentation = sdJwtService.createPresentation(fullToken, Set.of("inflow"));

        String[] parts = presentation.split("~", -1);
        // jwt + 1 disclosure + trailing empty = 3 parts
        assertThat(parts.length).isEqualTo(3);
        assertThat(parts[parts.length - 1]).isEmpty();
    }

    @Test
    void parseAndVerify_validToken_returnsPresentation() {
        List<IssuedClaim> claims = List.of(inflowClaim(30_000, true));
        String fullToken = sdJwtService.buildSdJwt(claims, "did:tshepo:bob", now(), later(), "jti-3");
        String presentation = sdJwtService.createPresentation(fullToken, Set.of("inflow"));

        SdJwtService.ParsedPresentation parsed = sdJwtService.parseAndVerifyPresentation(presentation);

        assertThat(parsed).isNotNull();
        assertThat(parsed.issuer()).isEqualTo("did:web:localhost");
        assertThat(parsed.subject()).isEqualTo("did:tshepo:bob");
        assertThat(parsed.disclosedClaims()).hasSize(1);
        assertThat(parsed.disclosedClaims().get(0).get("name")).isEqualTo("inflow");
    }

    @Test
    void parseAndVerify_hiddenClaimsNotVisible() {
        List<IssuedClaim> claims = List.of(inflowClaim(30_000, true), balanceClaim(50_000, true));
        String fullToken = sdJwtService.buildSdJwt(claims, "did:tshepo:carol", now(), later(), "jti-4");
        // Only reveal INFLOW, hide BALANCE
        String presentation = sdJwtService.createPresentation(fullToken, Set.of("inflow"));

        SdJwtService.ParsedPresentation parsed = sdJwtService.parseAndVerifyPresentation(presentation);

        assertThat(parsed).isNotNull();
        assertThat(parsed.disclosedClaims()).hasSize(1);
        boolean balanceVisible = parsed
            .disclosedClaims()
            .stream()
            .anyMatch(c -> "balance".equals(c.get("name")));
        assertThat(balanceVisible).isFalse();
    }

    @Test
    void parseAndVerify_tamperedToken_returnsNull() {
        List<IssuedClaim> claims = List.of(inflowClaim(30_000, true));
        String token = sdJwtService.buildSdJwt(claims, "did:tshepo:eve", now(), later(), "jti-5");
        // Tamper with the JWT signature (the part before the first ~)
        int tilde = token.indexOf('~');
        String jwtPart = token.substring(0, tilde);
        // Replace last 6 chars of the signature with zeros
        String tamperedJwt = jwtPart.substring(0, jwtPart.length() - 6) + "AAAAAA";
        String tampered = tamperedJwt + token.substring(tilde);

        SdJwtService.ParsedPresentation result = sdJwtService.parseAndVerifyPresentation(tampered);

        assertThat(result).isNull();
    }

    @Test
    void parseAndVerify_garbageInput_returnsNull() {
        assertThat(sdJwtService.parseAndVerifyPresentation("not-a-jwt")).isNull();
        assertThat(sdJwtService.parseAndVerifyPresentation("")).isNull();
    }

    // --- helpers ---

    private static Instant now() {
        return Instant.now();
    }

    private static Instant later() {
        return Instant.now().plus(180, ChronoUnit.DAYS);
    }

    private static IssuedClaim inflowClaim(double threshold, boolean met) {
        IssuedClaim c = new IssuedClaim();
        c.setClaimType(ClaimType.INFLOW);
        c.setOperator(ClaimOperator.GTE);
        c.setThreshold(BigDecimal.valueOf(threshold));
        c.setCurrency("ZAR");
        c.setPeriodMonths(6);
        c.setMet(met);
        return c;
    }

    private static IssuedClaim balanceClaim(double threshold, boolean met) {
        IssuedClaim c = new IssuedClaim();
        c.setClaimType(ClaimType.BALANCE);
        c.setOperator(ClaimOperator.GTE);
        c.setThreshold(BigDecimal.valueOf(threshold));
        c.setCurrency("ZAR");
        c.setPeriodMonths(1);
        c.setMet(met);
        return c;
    }
}
