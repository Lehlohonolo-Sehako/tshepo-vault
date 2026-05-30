package app.tshepo.service.mapper;

import app.tshepo.domain.BankConnection;
import app.tshepo.service.dto.BankConnectionDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link BankConnection} and its DTO {@link BankConnectionDTO}.
 */
@Mapper(componentModel = "spring")
public interface BankConnectionMapper extends EntityMapper<BankConnectionDTO, BankConnection> {}
