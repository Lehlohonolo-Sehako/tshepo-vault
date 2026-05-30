package app.tshepo.service.mapper;

import app.tshepo.domain.Credential;
import app.tshepo.service.dto.CredentialDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Credential} and its DTO {@link CredentialDTO}.
 */
@Mapper(componentModel = "spring")
public interface CredentialMapper extends EntityMapper<CredentialDTO, Credential> {}
