package app.tshepo.service.mapper;

import static app.tshepo.domain.VerificationEventAsserts.*;
import static app.tshepo.domain.VerificationEventTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VerificationEventMapperTest {

    private VerificationEventMapper verificationEventMapper;

    @BeforeEach
    void setUp() {
        verificationEventMapper = new VerificationEventMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getVerificationEventSample1();
        var actual = verificationEventMapper.toEntity(verificationEventMapper.toDto(expected));
        assertVerificationEventAllPropertiesEquals(expected, actual);
    }
}
