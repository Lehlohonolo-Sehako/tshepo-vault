package app.tshepo.domain;

import static app.tshepo.domain.BankConnectionTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import app.tshepo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class BankConnectionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(BankConnection.class);
        BankConnection bankConnection1 = getBankConnectionSample1();
        BankConnection bankConnection2 = new BankConnection();
        assertThat(bankConnection1).isNotEqualTo(bankConnection2);

        bankConnection2.setId(bankConnection1.getId());
        assertThat(bankConnection1).isEqualTo(bankConnection2);

        bankConnection2 = getBankConnectionSample2();
        assertThat(bankConnection1).isNotEqualTo(bankConnection2);
    }
}
