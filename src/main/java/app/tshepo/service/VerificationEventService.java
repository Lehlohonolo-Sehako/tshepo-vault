package app.tshepo.service;

import app.tshepo.domain.VerificationEvent;
import app.tshepo.repository.VerificationEventRepository;
import app.tshepo.service.dto.VerificationEventDTO;
import app.tshepo.service.mapper.VerificationEventMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link app.tshepo.domain.VerificationEvent}.
 */
@Service
@Transactional
public class VerificationEventService {

    private static final Logger LOG = LoggerFactory.getLogger(VerificationEventService.class);

    private final VerificationEventRepository verificationEventRepository;

    private final VerificationEventMapper verificationEventMapper;

    public VerificationEventService(
        VerificationEventRepository verificationEventRepository,
        VerificationEventMapper verificationEventMapper
    ) {
        this.verificationEventRepository = verificationEventRepository;
        this.verificationEventMapper = verificationEventMapper;
    }

    /**
     * Save a verificationEvent.
     *
     * @param verificationEventDTO the entity to save.
     * @return the persisted entity.
     */
    public VerificationEventDTO save(VerificationEventDTO verificationEventDTO) {
        LOG.debug("Request to save VerificationEvent : {}", verificationEventDTO);
        VerificationEvent verificationEvent = verificationEventMapper.toEntity(verificationEventDTO);
        verificationEvent = verificationEventRepository.save(verificationEvent);
        return verificationEventMapper.toDto(verificationEvent);
    }

    /**
     * Update a verificationEvent.
     *
     * @param verificationEventDTO the entity to save.
     * @return the persisted entity.
     */
    public VerificationEventDTO update(VerificationEventDTO verificationEventDTO) {
        LOG.debug("Request to update VerificationEvent : {}", verificationEventDTO);
        VerificationEvent verificationEvent = verificationEventMapper.toEntity(verificationEventDTO);
        verificationEvent = verificationEventRepository.save(verificationEvent);
        return verificationEventMapper.toDto(verificationEvent);
    }

    /**
     * Partially update a verificationEvent.
     *
     * @param verificationEventDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<VerificationEventDTO> partialUpdate(VerificationEventDTO verificationEventDTO) {
        LOG.debug("Request to partially update VerificationEvent : {}", verificationEventDTO);

        return verificationEventRepository
            .findById(verificationEventDTO.getId())
            .map(existingVerificationEvent -> {
                verificationEventMapper.partialUpdate(existingVerificationEvent, verificationEventDTO);

                return existingVerificationEvent;
            })
            .map(verificationEventRepository::save)
            .map(verificationEventMapper::toDto);
    }

    /**
     * Get all the verificationEvents with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<VerificationEventDTO> findAllWithEagerRelationships(Pageable pageable) {
        return verificationEventRepository.findAllWithEagerRelationships(pageable).map(verificationEventMapper::toDto);
    }

    /**
     * Get one verificationEvent by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<VerificationEventDTO> findOne(Long id) {
        LOG.debug("Request to get VerificationEvent : {}", id);
        return verificationEventRepository.findOneWithEagerRelationships(id).map(verificationEventMapper::toDto);
    }

    /**
     * Delete the verificationEvent by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete VerificationEvent : {}", id);
        verificationEventRepository.deleteById(id);
    }
}
