package app.tshepo.domain;

import static app.tshepo.domain.CredentialTestSamples.*;
import static app.tshepo.domain.IssuedClaimTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import app.tshepo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class IssuedClaimTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(IssuedClaim.class);
        IssuedClaim issuedClaim1 = getIssuedClaimSample1();
        IssuedClaim issuedClaim2 = new IssuedClaim();
        assertThat(issuedClaim1).isNotEqualTo(issuedClaim2);

        issuedClaim2.setId(issuedClaim1.getId());
        assertThat(issuedClaim1).isEqualTo(issuedClaim2);

        issuedClaim2 = getIssuedClaimSample2();
        assertThat(issuedClaim1).isNotEqualTo(issuedClaim2);
    }

    @Test
    void credentialTest() {
        IssuedClaim issuedClaim = getIssuedClaimRandomSampleGenerator();
        Credential credentialBack = getCredentialRandomSampleGenerator();

        issuedClaim.setCredential(credentialBack);
        assertThat(issuedClaim.getCredential()).isEqualTo(credentialBack);

        issuedClaim.credential(null);
        assertThat(issuedClaim.getCredential()).isNull();
    }
}
