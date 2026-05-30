package app.tshepo.domain;

import static app.tshepo.domain.CredentialTestSamples.*;
import static app.tshepo.domain.IssuedClaimTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import app.tshepo.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CredentialTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Credential.class);
        Credential credential1 = getCredentialSample1();
        Credential credential2 = new Credential();
        assertThat(credential1).isNotEqualTo(credential2);

        credential2.setId(credential1.getId());
        assertThat(credential1).isEqualTo(credential2);

        credential2 = getCredentialSample2();
        assertThat(credential1).isNotEqualTo(credential2);
    }

    @Test
    void claimsTest() {
        Credential credential = getCredentialRandomSampleGenerator();
        IssuedClaim issuedClaimBack = getIssuedClaimRandomSampleGenerator();

        credential.addClaims(issuedClaimBack);
        assertThat(credential.getClaimses()).containsOnly(issuedClaimBack);
        assertThat(issuedClaimBack.getCredential()).isEqualTo(credential);

        credential.removeClaims(issuedClaimBack);
        assertThat(credential.getClaimses()).doesNotContain(issuedClaimBack);
        assertThat(issuedClaimBack.getCredential()).isNull();

        credential.claimses(new HashSet<>(Set.of(issuedClaimBack)));
        assertThat(credential.getClaimses()).containsOnly(issuedClaimBack);
        assertThat(issuedClaimBack.getCredential()).isEqualTo(credential);

        credential.setClaimses(new HashSet<>());
        assertThat(credential.getClaimses()).doesNotContain(issuedClaimBack);
        assertThat(issuedClaimBack.getCredential()).isNull();
    }
}
