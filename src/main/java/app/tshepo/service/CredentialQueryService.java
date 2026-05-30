package app.tshepo.service;

import app.tshepo.domain.*; // for static metamodels
import app.tshepo.domain.Credential;
import app.tshepo.repository.CredentialRepository;
import app.tshepo.service.criteria.CredentialCriteria;
import app.tshepo.service.dto.CredentialDTO;
import app.tshepo.service.mapper.CredentialMapper;
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
 * Service for executing complex queries for {@link Credential} entities in the database.
 * The main input is a {@link CredentialCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link CredentialDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class CredentialQueryService extends QueryService<Credential> {

    private static final Logger LOG = LoggerFactory.getLogger(CredentialQueryService.class);

    private final CredentialRepository credentialRepository;

    private final CredentialMapper credentialMapper;

    public CredentialQueryService(CredentialRepository credentialRepository, CredentialMapper credentialMapper) {
        this.credentialRepository = credentialRepository;
        this.credentialMapper = credentialMapper;
    }

    /**
     * Return a {@link Page} of {@link CredentialDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<CredentialDTO> findByCriteria(CredentialCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Credential> specification = createSpecification(criteria);
        return credentialRepository.findAll(specification, page).map(credentialMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(CredentialCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Credential> specification = createSpecification(criteria);
        return credentialRepository.count(specification);
    }

    /**
     * Function to convert {@link CredentialCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Credential> createSpecification(CredentialCriteria criteria) {
        Specification<Credential> specification = Specification.unrestricted();
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                buildRangeSpecification(criteria.getId(), Credential_.id),
                buildStringSpecification(criteria.getHolderLogin(), Credential_.holderLogin),
                buildStringSpecification(criteria.getTitle(), Credential_.title),
                buildStringSpecification(criteria.getPurpose(), Credential_.purpose),
                buildSpecification(criteria.getStatus(), Credential_.status),
                buildRangeSpecification(criteria.getIssuedAt(), Credential_.issuedAt),
                buildRangeSpecification(criteria.getExpiresAt(), Credential_.expiresAt),
                buildStringSpecification(criteria.getIssuerDid(), Credential_.issuerDid),
                buildStringSpecification(criteria.getClaimsSummary(), Credential_.claimsSummary),
                buildStringSpecification(criteria.getVcRef(), Credential_.vcRef),
                buildSpecification(criteria.getClaimsId(), root -> root.join(Credential_.claimses, JoinType.LEFT).get(IssuedClaim_.id))
            );
        }
        return specification;
    }
}
