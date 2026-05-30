package app.tshepo.service;

import app.tshepo.domain.*; // for static metamodels
import app.tshepo.domain.VerificationEvent;
import app.tshepo.repository.VerificationEventRepository;
import app.tshepo.service.criteria.VerificationEventCriteria;
import app.tshepo.service.dto.VerificationEventDTO;
import app.tshepo.service.mapper.VerificationEventMapper;
import jakarta.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link VerificationEvent} entities in the database.
 * The main input is a {@link VerificationEventCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link VerificationEventDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class VerificationEventQueryService extends QueryService<VerificationEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(VerificationEventQueryService.class);

    private final VerificationEventRepository verificationEventRepository;

    private final VerificationEventMapper verificationEventMapper;

    public VerificationEventQueryService(
        VerificationEventRepository verificationEventRepository,
        VerificationEventMapper verificationEventMapper
    ) {
        this.verificationEventRepository = verificationEventRepository;
        this.verificationEventMapper = verificationEventMapper;
    }

    /**
     * Return a {@link Page} of {@link VerificationEventDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<VerificationEventDTO> findByCriteria(VerificationEventCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<VerificationEvent> specification = createSpecification(criteria);
        return verificationEventRepository.findAll(specification, page).map(verificationEventMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(VerificationEventCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<VerificationEvent> specification = createSpecification(criteria);
        return verificationEventRepository.count(specification);
    }

    /**
     * Function to convert {@link VerificationEventCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<VerificationEvent> createSpecification(VerificationEventCriteria criteria) {
        Specification<VerificationEvent> specification = Specification.unrestricted();
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                buildRangeSpecification(criteria.getId(), VerificationEvent_.id),
                buildRangeSpecification(criteria.getVerifiedAt(), VerificationEvent_.verifiedAt),
                buildSpecification(criteria.getResult(), VerificationEvent_.result),
                buildStringSpecification(criteria.getCredentialRef(), VerificationEvent_.credentialRef),
                buildSpecification(criteria.getApiKeyId(), root ->
                    root.join(VerificationEvent_.apiKey, JoinType.LEFT).get(VerifierApiKey_.id)
                )
            );
        }
        return specification;
    }
}
