package app.tshepo.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import app.tshepo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CredentialDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(CredentialDTO.class);
        CredentialDTO credentialDTO1 = new CredentialDTO();
        credentialDTO1.setId(1L);
        CredentialDTO credentialDTO2 = new CredentialDTO();
        assertThat(credentialDTO1).isNotEqualTo(credentialDTO2);
        credentialDTO2.setId(credentialDTO1.getId());
        assertThat(credentialDTO1).isEqualTo(credentialDTO2);
        credentialDTO2.setId(2L);
        assertThat(credentialDTO1).isNotEqualTo(credentialDTO2);
        credentialDTO1.setId(null);
        assertThat(credentialDTO1).isNotEqualTo(credentialDTO2);
    }
}
