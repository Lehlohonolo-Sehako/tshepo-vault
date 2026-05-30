package app.tshepo.service.mapper;

import app.tshepo.domain.Credential;
import app.tshepo.domain.IssuedClaim;
import app.tshepo.service.dto.CredentialDTO;
import app.tshepo.service.dto.IssuedClaimDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link IssuedClaim} and its DTO {@link IssuedClaimDTO}.
 */
@Mapper(componentModel = "spring")
public interface IssuedClaimMapper extends EntityMapper<IssuedClaimDTO, IssuedClaim> {
    @Mapping(target = "credential", source = "credential", qualifiedByName = "credentialTitle")
    IssuedClaimDTO toDto(IssuedClaim s);

    @Named("credentialTitle")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    CredentialDTO toDtoCredentialTitle(Credential credential);
}
