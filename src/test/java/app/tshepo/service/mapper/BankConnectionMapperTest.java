package app.tshepo.service.mapper;

import static app.tshepo.domain.BankConnectionAsserts.*;
import static app.tshepo.domain.BankConnectionTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BankConnectionMapperTest {

    private BankConnectionMapper bankConnectionMapper;

    @BeforeEach
    void setUp() {
        bankConnectionMapper = new BankConnectionMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getBankConnectionSample1();
        var actual = bankConnectionMapper.toEntity(bankConnectionMapper.toDto(expected));
        assertBankConnectionAllPropertiesEquals(expected, actual);
    }
}
