package app.tshepo.service;

import app.tshepo.domain.BankConnection;
import app.tshepo.repository.BankConnectionRepository;
import app.tshepo.service.dto.BankConnectionDTO;
import app.tshepo.service.mapper.BankConnectionMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link app.tshepo.domain.BankConnection}.
 */
@Service
@Transactional
public class BankConnectionService {

    private static final Logger LOG = LoggerFactory.getLogger(BankConnectionService.class);

    private final BankConnectionRepository bankConnectionRepository;

    private final BankConnectionMapper bankConnectionMapper;

    public BankConnectionService(BankConnectionRepository bankConnectionRepository, BankConnectionMapper bankConnectionMapper) {
        this.bankConnectionRepository = bankConnectionRepository;
        this.bankConnectionMapper = bankConnectionMapper;
    }

    /**
     * Save a bankConnection.
     *
     * @param bankConnectionDTO the entity to save.
     * @return the persisted entity.
     */
    public BankConnectionDTO save(BankConnectionDTO bankConnectionDTO) {
        LOG.debug("Request to save BankConnection : {}", bankConnectionDTO);
        BankConnection bankConnection = bankConnectionMapper.toEntity(bankConnectionDTO);
        bankConnection = bankConnectionRepository.save(bankConnection);
        return bankConnectionMapper.toDto(bankConnection);
    }

    /**
     * Update a bankConnection.
     *
     * @param bankConnectionDTO the entity to save.
     * @return the persisted entity.
     */
    public BankConnectionDTO update(BankConnectionDTO bankConnectionDTO) {
        LOG.debug("Request to update BankConnection : {}", bankConnectionDTO);
        BankConnection bankConnection = bankConnectionMapper.toEntity(bankConnectionDTO);
        bankConnection = bankConnectionRepository.save(bankConnection);
        return bankConnectionMapper.toDto(bankConnection);
    }

    /**
     * Partially update a bankConnection.
     *
     * @param bankConnectionDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<BankConnectionDTO> partialUpdate(BankConnectionDTO bankConnectionDTO) {
        LOG.debug("Request to partially update BankConnection : {}", bankConnectionDTO);

        return bankConnectionRepository
            .findById(bankConnectionDTO.getId())
            .map(existingBankConnection -> {
                bankConnectionMapper.partialUpdate(existingBankConnection, bankConnectionDTO);

                return existingBankConnection;
            })
            .map(bankConnectionRepository::save)
            .map(bankConnectionMapper::toDto);
    }

    /**
     * Get all the bankConnections.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<BankConnectionDTO> findAll() {
        LOG.debug("Request to get all BankConnections");
        return bankConnectionRepository
            .findAll()
            .stream()
            .map(bankConnectionMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get one bankConnection by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<BankConnectionDTO> findOne(Long id) {
        LOG.debug("Request to get BankConnection : {}", id);
        return bankConnectionRepository.findById(id).map(bankConnectionMapper::toDto);
    }

    /**
     * Delete the bankConnection by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete BankConnection : {}", id);
        bankConnectionRepository.deleteById(id);
    }
}
