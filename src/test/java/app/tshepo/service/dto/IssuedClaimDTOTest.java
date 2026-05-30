package app.tshepo.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import app.tshepo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class IssuedClaimDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(IssuedClaimDTO.class);
        IssuedClaimDTO issuedClaimDTO1 = new IssuedClaimDTO();
        issuedClaimDTO1.setId(1L);
        IssuedClaimDTO issuedClaimDTO2 = new IssuedClaimDTO();
        assertThat(issuedClaimDTO1).isNotEqualTo(issuedClaimDTO2);
        issuedClaimDTO2.setId(issuedClaimDTO1.getId());
        assertThat(issuedClaimDTO1).isEqualTo(issuedClaimDTO2);
        issuedClaimDTO2.setId(2L);
        assertThat(issuedClaimDTO1).isNotEqualTo(issuedClaimDTO2);
        issuedClaimDTO1.setId(null);
        assertThat(issuedClaimDTO1).isNotEqualTo(issuedClaimDTO2);
    }
}
