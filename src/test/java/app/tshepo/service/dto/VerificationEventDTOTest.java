package app.tshepo.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import app.tshepo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class VerificationEventDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(VerificationEventDTO.class);
        VerificationEventDTO verificationEventDTO1 = new VerificationEventDTO();
        verificationEventDTO1.setId(1L);
        VerificationEventDTO verificationEventDTO2 = new VerificationEventDTO();
        assertThat(verificationEventDTO1).isNotEqualTo(verificationEventDTO2);
        verificationEventDTO2.setId(verificationEventDTO1.getId());
        assertThat(verificationEventDTO1).isEqualTo(verificationEventDTO2);
        verificationEventDTO2.setId(2L);
        assertThat(verificationEventDTO1).isNotEqualTo(verificationEventDTO2);
        verificationEventDTO1.setId(null);
        assertThat(verificationEventDTO1).isNotEqualTo(verificationEventDTO2);
    }
}
