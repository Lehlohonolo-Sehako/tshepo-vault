package app.tshepo.service.mapper;

import static app.tshepo.domain.IssuedClaimAsserts.*;
import static app.tshepo.domain.IssuedClaimTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IssuedClaimMapperTest {

    private IssuedClaimMapper issuedClaimMapper;

    @BeforeEach
    void setUp() {
        issuedClaimMapper = new IssuedClaimMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getIssuedClaimSample1();
        var actual = issuedClaimMapper.toEntity(issuedClaimMapper.toDto(expected));
        assertIssuedClaimAllPropertiesEquals(expected, actual);
    }
}
