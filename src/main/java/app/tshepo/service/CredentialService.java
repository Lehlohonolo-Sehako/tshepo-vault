package app.tshepo.service;

import app.tshepo.domain.Credential;
import app.tshepo.repository.CredentialRepository;
import app.tshepo.service.dto.CredentialDTO;
import app.tshepo.service.mapper.CredentialMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link app.tshepo.domain.Credential}.
 */
@Service
@Transactional
public class CredentialService {

    private static final Logger LOG = LoggerFactory.getLogger(CredentialService.class);

    private final CredentialRepository credentialRepository;

    private final CredentialMapper credentialMapper;

    public CredentialService(CredentialRepository credentialRepository, CredentialMapper credentialMapper) {
        this.credentialRepository = credentialRepository;
        this.credentialMapper = credentialMapper;
    }

    /**
     * Save a credential.
     *
     * @param credentialDTO the entity to save.
     * @return the persisted entity.
     */
    public CredentialDTO save(CredentialDTO credentialDTO) {
        LOG.debug("Request to save Credential : {}", credentialDTO);
        Credential credential = credentialMapper.toEntity(credentialDTO);
        credential = credentialRepository.save(credential);
        return credentialMapper.toDto(credential);
    }

    /**
     * Update a credential.
     *
     * @param credentialDTO the entity to save.
     * @return the persisted entity.
     */
    public CredentialDTO update(CredentialDTO credentialDTO) {
        LOG.debug("Request to update Credential : {}", credentialDTO);
        Credential credential = credentialMapper.toEntity(credentialDTO);
        credential = credentialRepository.save(credential);
        return credentialMapper.toDto(credential);
    }

    /**
     * Partially update a credential.
     *
     * @param credentialDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<CredentialDTO> partialUpdate(CredentialDTO credentialDTO) {
        LOG.debug("Request to partially update Credential : {}", credentialDTO);

        return credentialRepository
            .findById(credentialDTO.getId())
            .map(existingCredential -> {
                credentialMapper.partialUpdate(existingCredential, credentialDTO);

                return existingCredential;
            })
            .map(credentialRepository::save)
            .map(credentialMapper::toDto);
    }

    /**
     * Get one credential by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<CredentialDTO> findOne(Long id) {
        LOG.debug("Request to get Credential : {}", id);
        return credentialRepository.findById(id).map(credentialMapper::toDto);
    }

    /**
     * Delete the credential by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Credential : {}", id);
        credentialRepository.deleteById(id);
    }
}
