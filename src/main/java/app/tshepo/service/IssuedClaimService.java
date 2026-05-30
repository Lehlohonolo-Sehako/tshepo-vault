package app.tshepo.service;

import app.tshepo.domain.IssuedClaim;
import app.tshepo.repository.IssuedClaimRepository;
import app.tshepo.service.dto.IssuedClaimDTO;
import app.tshepo.service.mapper.IssuedClaimMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link app.tshepo.domain.IssuedClaim}.
 */
@Service
@Transactional
public class IssuedClaimService {

    private static final Logger LOG = LoggerFactory.getLogger(IssuedClaimService.class);

    private final IssuedClaimRepository issuedClaimRepository;

    private final IssuedClaimMapper issuedClaimMapper;

    public IssuedClaimService(IssuedClaimRepository issuedClaimRepository, IssuedClaimMapper issuedClaimMapper) {
        this.issuedClaimRepository = issuedClaimRepository;
        this.issuedClaimMapper = issuedClaimMapper;
    }

    /**
     * Save a issuedClaim.
     *
     * @param issuedClaimDTO the entity to save.
     * @return the persisted entity.
     */
    public IssuedClaimDTO save(IssuedClaimDTO issuedClaimDTO) {
        LOG.debug("Request to save IssuedClaim : {}", issuedClaimDTO);
        IssuedClaim issuedClaim = issuedClaimMapper.toEntity(issuedClaimDTO);
        issuedClaim = issuedClaimRepository.save(issuedClaim);
        return issuedClaimMapper.toDto(issuedClaim);
    }

    /**
     * Update a issuedClaim.
     *
     * @param issuedClaimDTO the entity to save.
     * @return the persisted entity.
     */
    public IssuedClaimDTO update(IssuedClaimDTO issuedClaimDTO) {
        LOG.debug("Request to update IssuedClaim : {}", issuedClaimDTO);
        IssuedClaim issuedClaim = issuedClaimMapper.toEntity(issuedClaimDTO);
        issuedClaim = issuedClaimRepository.save(issuedClaim);
        return issuedClaimMapper.toDto(issuedClaim);
    }

    /**
     * Partially update a issuedClaim.
     *
     * @param issuedClaimDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<IssuedClaimDTO> partialUpdate(IssuedClaimDTO issuedClaimDTO) {
        LOG.debug("Request to partially update IssuedClaim : {}", issuedClaimDTO);

        return issuedClaimRepository
            .findById(issuedClaimDTO.getId())
            .map(existingIssuedClaim -> {
                issuedClaimMapper.partialUpdate(existingIssuedClaim, issuedClaimDTO);

                return existingIssuedClaim;
            })
            .map(issuedClaimRepository::save)
            .map(issuedClaimMapper::toDto);
    }

    /**
     * Get all the issuedClaims.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<IssuedClaimDTO> findAll() {
        LOG.debug("Request to get all IssuedClaims");
        return issuedClaimRepository.findAll().stream().map(issuedClaimMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get all the issuedClaims with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<IssuedClaimDTO> findAllWithEagerRelationships(Pageable pageable) {
        return issuedClaimRepository.findAllWithEagerRelationships(pageable).map(issuedClaimMapper::toDto);
    }

    /**
     * Get one issuedClaim by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<IssuedClaimDTO> findOne(Long id) {
        LOG.debug("Request to get IssuedClaim : {}", id);
        return issuedClaimRepository.findOneWithEagerRelationships(id).map(issuedClaimMapper::toDto);
    }

    /**
     * Delete the issuedClaim by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete IssuedClaim : {}", id);
        issuedClaimRepository.deleteById(id);
    }
}
