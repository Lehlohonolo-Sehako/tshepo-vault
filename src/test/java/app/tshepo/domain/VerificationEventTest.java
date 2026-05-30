package app.tshepo.domain;

import static app.tshepo.domain.VerificationEventTestSamples.*;
import static app.tshepo.domain.VerifierApiKeyTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import app.tshepo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class VerificationEventTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(VerificationEvent.class);
        VerificationEvent verificationEvent1 = getVerificationEventSample1();
        VerificationEvent verificationEvent2 = new VerificationEvent();
        assertThat(verificationEvent1).isNotEqualTo(verificationEvent2);

        verificationEvent2.setId(verificationEvent1.getId());
        assertThat(verificationEvent1).isEqualTo(verificationEvent2);

        verificationEvent2 = getVerificationEventSample2();
        assertThat(verificationEvent1).isNotEqualTo(verificationEvent2);
    }

    @Test
    void apiKeyTest() {
        VerificationEvent verificationEvent = getVerificationEventRandomSampleGenerator();
        VerifierApiKey verifierApiKeyBack = getVerifierApiKeyRandomSampleGenerator();

        verificationEvent.setApiKey(verifierApiKeyBack);
        assertThat(verificationEvent.getApiKey()).isEqualTo(verifierApiKeyBack);

        verificationEvent.apiKey(null);
        assertThat(verificationEvent.getApiKey()).isNull();
    }
}
