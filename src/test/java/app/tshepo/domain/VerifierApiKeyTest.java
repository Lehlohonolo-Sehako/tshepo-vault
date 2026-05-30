package app.tshepo.domain;

import static app.tshepo.domain.VerifierApiKeyTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import app.tshepo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class VerifierApiKeyTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(VerifierApiKey.class);
        VerifierApiKey verifierApiKey1 = getVerifierApiKeySample1();
        VerifierApiKey verifierApiKey2 = new VerifierApiKey();
        assertThat(verifierApiKey1).isNotEqualTo(verifierApiKey2);

        verifierApiKey2.setId(verifierApiKey1.getId());
        assertThat(verifierApiKey1).isEqualTo(verifierApiKey2);

        verifierApiKey2 = getVerifierApiKeySample2();
        assertThat(verifierApiKey1).isNotEqualTo(verifierApiKey2);
    }
}
