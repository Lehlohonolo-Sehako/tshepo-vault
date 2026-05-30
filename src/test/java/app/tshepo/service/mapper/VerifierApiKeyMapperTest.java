package app.tshepo.service.mapper;

import static app.tshepo.domain.VerifierApiKeyAsserts.*;
import static app.tshepo.domain.VerifierApiKeyTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VerifierApiKeyMapperTest {

    private VerifierApiKeyMapper verifierApiKeyMapper;

    @BeforeEach
    void setUp() {
        verifierApiKeyMapper = new VerifierApiKeyMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getVerifierApiKeySample1();
        var actual = verifierApiKeyMapper.toEntity(verifierApiKeyMapper.toDto(expected));
        assertVerifierApiKeyAllPropertiesEquals(expected, actual);
    }
}
