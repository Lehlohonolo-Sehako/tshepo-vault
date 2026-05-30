package app.tshepo.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import app.tshepo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class BankConnectionDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(BankConnectionDTO.class);
        BankConnectionDTO bankConnectionDTO1 = new BankConnectionDTO();
        bankConnectionDTO1.setId(1L);
        BankConnectionDTO bankConnectionDTO2 = new BankConnectionDTO();
        assertThat(bankConnectionDTO1).isNotEqualTo(bankConnectionDTO2);
        bankConnectionDTO2.setId(bankConnectionDTO1.getId());
        assertThat(bankConnectionDTO1).isEqualTo(bankConnectionDTO2);
        bankConnectionDTO2.setId(2L);
        assertThat(bankConnectionDTO1).isNotEqualTo(bankConnectionDTO2);
        bankConnectionDTO1.setId(null);
        assertThat(bankConnectionDTO1).isNotEqualTo(bankConnectionDTO2);
    }
}
