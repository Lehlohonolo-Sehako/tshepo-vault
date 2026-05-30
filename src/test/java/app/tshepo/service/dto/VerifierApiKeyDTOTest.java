package app.tshepo.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import app.tshepo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class VerifierApiKeyDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(VerifierApiKeyDTO.class);
        VerifierApiKeyDTO verifierApiKeyDTO1 = new VerifierApiKeyDTO();
        verifierApiKeyDTO1.setId(1L);
        VerifierApiKeyDTO verifierApiKeyDTO2 = new VerifierApiKeyDTO();
        assertThat(verifierApiKeyDTO1).isNotEqualTo(verifierApiKeyDTO2);
        verifierApiKeyDTO2.setId(verifierApiKeyDTO1.getId());
        assertThat(verifierApiKeyDTO1).isEqualTo(verifierApiKeyDTO2);
        verifierApiKeyDTO2.setId(2L);
        assertThat(verifierApiKeyDTO1).isNotEqualTo(verifierApiKeyDTO2);
        verifierApiKeyDTO1.setId(null);
        assertThat(verifierApiKeyDTO1).isNotEqualTo(verifierApiKeyDTO2);
    }
}
