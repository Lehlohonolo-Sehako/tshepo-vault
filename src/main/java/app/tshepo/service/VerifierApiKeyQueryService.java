package app.tshepo.service;

import app.tshepo.domain.*; // for static metamodels
import app.tshepo.domain.VerifierApiKey;
import app.tshepo.repository.VerifierApiKeyRepository;
import app.tshepo.service.criteria.VerifierApiKeyCriteria;
import app.tshepo.service.dto.VerifierApiKeyDTO;
import app.tshepo.service.mapper.VerifierApiKeyMapper;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link VerifierApiKey} entities in the database.
 * The main input is a {@link VerifierApiKeyCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link VerifierApiKeyDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class VerifierApiKeyQueryService extends QueryService<VerifierApiKey> {

    private static final Logger LOG = LoggerFactory.getLogger(VerifierApiKeyQueryService.class);

    private final VerifierApiKeyRepository verifierApiKeyRepository;

    private final VerifierApiKeyMapper verifierApiKeyMapper;

    public VerifierApiKeyQueryService(VerifierApiKeyRepository verifierApiKeyRepository, VerifierApiKeyMapper verifierApiKeyMapper) {
        this.verifierApiKeyRepository = verifierApiKeyRepository;
        this.verifierApiKeyMapper = verifierApiKeyMapper;
    }

    /**
     * Return a {@link List} of {@link VerifierApiKeyDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<VerifierApiKeyDTO> findByCriteria(VerifierApiKeyCriteria criteria) {
        LOG.debug("find by criteria : {}", criteria);
        final Specification<VerifierApiKey> specification = createSpecification(criteria);
        return verifierApiKeyMapper.toDto(verifierApiKeyRepository.findAll(specification));
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(VerifierApiKeyCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<VerifierApiKey> specification = createSpecification(criteria);
        return verifierApiKeyRepository.count(specification);
    }

    /**
     * Function to convert {@link VerifierApiKeyCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<VerifierApiKey> createSpecification(VerifierApiKeyCriteria criteria) {
        Specification<VerifierApiKey> specification = Specification.unrestricted();
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                buildRangeSpecification(criteria.getId(), VerifierApiKey_.id),
                buildStringSpecification(criteria.getOwnerLogin(), VerifierApiKey_.ownerLogin),
                buildStringSpecification(criteria.getLabel(), VerifierApiKey_.label),
                buildStringSpecification(criteria.getKeyHash(), VerifierApiKey_.keyHash),
                buildSpecification(criteria.getActive(), VerifierApiKey_.active),
                buildRangeSpecification(criteria.getCallCount(), VerifierApiKey_.callCount),
                buildRangeSpecification(criteria.getCreatedAt(), VerifierApiKey_.createdAt)
            );
        }
        return specification;
    }
}
