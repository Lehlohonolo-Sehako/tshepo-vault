package app.tshepo.service.mapper;

import app.tshepo.domain.VerifierApiKey;
import app.tshepo.service.dto.VerifierApiKeyDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link VerifierApiKey} and its DTO {@link VerifierApiKeyDTO}.
 */
@Mapper(componentModel = "spring")
public interface VerifierApiKeyMapper extends EntityMapper<VerifierApiKeyDTO, VerifierApiKey> {}
