package app.tshepo.service.mapper;

import static app.tshepo.domain.CredentialAsserts.*;
import static app.tshepo.domain.CredentialTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CredentialMapperTest {

    private CredentialMapper credentialMapper;

    @BeforeEach
    void setUp() {
        credentialMapper = new CredentialMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getCredentialSample1();
        var actual = credentialMapper.toEntity(credentialMapper.toDto(expected));
        assertCredentialAllPropertiesEquals(expected, actual);
    }
}
