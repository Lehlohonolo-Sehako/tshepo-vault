package app.tshepo.service;

import app.tshepo.domain.VerifierApiKey;
import app.tshepo.repository.VerifierApiKeyRepository;
import app.tshepo.service.dto.VerifierApiKeyDTO;
import app.tshepo.service.mapper.VerifierApiKeyMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link app.tshepo.domain.VerifierApiKey}.
 */
@Service
@Transactional
public class VerifierApiKeyService {

    private static final Logger LOG = LoggerFactory.getLogger(VerifierApiKeyService.class);

    private final VerifierApiKeyRepository verifierApiKeyRepository;

    private final VerifierApiKeyMapper verifierApiKeyMapper;

    public VerifierApiKeyService(VerifierApiKeyRepository verifierApiKeyRepository, VerifierApiKeyMapper verifierApiKeyMapper) {
        this.verifierApiKeyRepository = verifierApiKeyRepository;
        this.verifierApiKeyMapper = verifierApiKeyMapper;
    }

    /**
     * Save a verifierApiKey.
     *
     * @param verifierApiKeyDTO the entity to save.
     * @return the persisted entity.
     */
    public VerifierApiKeyDTO save(VerifierApiKeyDTO verifierApiKeyDTO) {
        LOG.debug("Request to save VerifierApiKey : {}", verifierApiKeyDTO);
        VerifierApiKey verifierApiKey = verifierApiKeyMapper.toEntity(verifierApiKeyDTO);
        verifierApiKey = verifierApiKeyRepository.save(verifierApiKey);
        return verifierApiKeyMapper.toDto(verifierApiKey);
    }

    /**
     * Update a verifierApiKey.
     *
     * @param verifierApiKeyDTO the entity to save.
     * @return the persisted entity.
     */
    public VerifierApiKeyDTO update(VerifierApiKeyDTO verifierApiKeyDTO) {
        LOG.debug("Request to update VerifierApiKey : {}", verifierApiKeyDTO);
        VerifierApiKey verifierApiKey = verifierApiKeyMapper.toEntity(verifierApiKeyDTO);
        verifierApiKey = verifierApiKeyRepository.save(verifierApiKey);
        return verifierApiKeyMapper.toDto(verifierApiKey);
    }

    /**
     * Partially update a verifierApiKey.
     *
     * @param verifierApiKeyDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<VerifierApiKeyDTO> partialUpdate(VerifierApiKeyDTO verifierApiKeyDTO) {
        LOG.debug("Request to partially update VerifierApiKey : {}", verifierApiKeyDTO);

        return verifierApiKeyRepository
            .findById(verifierApiKeyDTO.getId())
            .map(existingVerifierApiKey -> {
                verifierApiKeyMapper.partialUpdate(existingVerifierApiKey, verifierApiKeyDTO);

                return existingVerifierApiKey;
            })
            .map(verifierApiKeyRepository::save)
            .map(verifierApiKeyMapper::toDto);
    }

    /**
     * Get one verifierApiKey by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<VerifierApiKeyDTO> findOne(Long id) {
        LOG.debug("Request to get VerifierApiKey : {}", id);
        return verifierApiKeyRepository.findById(id).map(verifierApiKeyMapper::toDto);
    }

    /**
     * Delete the verifierApiKey by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete VerifierApiKey : {}", id);
        verifierApiKeyRepository.deleteById(id);
    }
}
