package app.tshepo.service.mapper;

import app.tshepo.domain.VerificationEvent;
import app.tshepo.domain.VerifierApiKey;
import app.tshepo.service.dto.VerificationEventDTO;
import app.tshepo.service.dto.VerifierApiKeyDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link VerificationEvent} and its DTO {@link VerificationEventDTO}.
 */
@Mapper(componentModel = "spring")
public interface VerificationEventMapper extends EntityMapper<VerificationEventDTO, VerificationEvent> {
    @Mapping(target = "apiKey", source = "apiKey", qualifiedByName = "verifierApiKeyLabel")
    VerificationEventDTO toDto(VerificationEvent s);

    @Named("verifierApiKeyLabel")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "label", source = "label")
    VerifierApiKeyDTO toDtoVerifierApiKeyLabel(VerifierApiKey verifierApiKey);
}
