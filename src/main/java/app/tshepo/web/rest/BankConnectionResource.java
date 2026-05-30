package app.tshepo.web.rest;

import app.tshepo.repository.BankConnectionRepository;
import app.tshepo.service.BankConnectionService;
import app.tshepo.service.dto.BankConnectionDTO;
import app.tshepo.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link app.tshepo.domain.BankConnection}.
 */
@RestController
@RequestMapping("/api/bank-connections")
public class BankConnectionResource {

    private static final Logger LOG = LoggerFactory.getLogger(BankConnectionResource.class);

    private static final String ENTITY_NAME = "bankConnection";

    @Value("${jhipster.clientApp.name:tshepoVault}")
    private String applicationName;

    private final BankConnectionService bankConnectionService;

    private final BankConnectionRepository bankConnectionRepository;

    public BankConnectionResource(BankConnectionService bankConnectionService, BankConnectionRepository bankConnectionRepository) {
        this.bankConnectionService = bankConnectionService;
        this.bankConnectionRepository = bankConnectionRepository;
    }

    /**
     * {@code POST  /bank-connections} : Create a new bankConnection.
     *
     * @param bankConnectionDTO the bankConnectionDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new bankConnectionDTO, or with status {@code 400 (Bad Request)} if the bankConnection has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<BankConnectionDTO> createBankConnection(@Valid @RequestBody BankConnectionDTO bankConnectionDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save BankConnection : {}", bankConnectionDTO);
        if (bankConnectionDTO.getId() != null) {
            throw new BadRequestAlertException("A new bankConnection cannot already have an ID", ENTITY_NAME, "idexists");
        }
        bankConnectionDTO = bankConnectionService.save(bankConnectionDTO);
        return ResponseEntity.created(new URI("/api/bank-connections/" + bankConnectionDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, bankConnectionDTO.getId().toString()))
            .body(bankConnectionDTO);
    }

    /**
     * {@code PUT  /bank-connections/:id} : Updates an existing bankConnection.
     *
     * @param id the id of the bankConnectionDTO to save.
     * @param bankConnectionDTO the bankConnectionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated bankConnectionDTO,
     * or with status {@code 400 (Bad Request)} if the bankConnectionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the bankConnectionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<BankConnectionDTO> updateBankConnection(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody BankConnectionDTO bankConnectionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update BankConnection : {}, {}", id, bankConnectionDTO);
        if (bankConnectionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, bankConnectionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!bankConnectionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        bankConnectionDTO = bankConnectionService.update(bankConnectionDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, bankConnectionDTO.getId().toString()))
            .body(bankConnectionDTO);
    }

    /**
     * {@code PATCH  /bank-connections/:id} : Partial updates given fields of an existing bankConnection, field will ignore if it is null
     *
     * @param id the id of the bankConnectionDTO to save.
     * @param bankConnectionDTO the bankConnectionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated bankConnectionDTO,
     * or with status {@code 400 (Bad Request)} if the bankConnectionDTO is not valid,
     * or with status {@code 404 (Not Found)} if the bankConnectionDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the bankConnectionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<BankConnectionDTO> partialUpdateBankConnection(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody BankConnectionDTO bankConnectionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update BankConnection partially : {}, {}", id, bankConnectionDTO);
        if (bankConnectionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, bankConnectionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!bankConnectionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<BankConnectionDTO> result = bankConnectionService.partialUpdate(bankConnectionDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, bankConnectionDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /bank-connections} : get all the Bank Connections.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Bank Connections in body.
     */
    @GetMapping("")
    public List<BankConnectionDTO> getAllBankConnections() {
        LOG.debug("REST request to get all BankConnections");
        return bankConnectionService.findAll();
    }

    /**
     * {@code GET  /bank-connections/:id} : get the "id" bankConnection.
     *
     * @param id the id of the bankConnectionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the bankConnectionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BankConnectionDTO> getBankConnection(@PathVariable("id") Long id) {
        LOG.debug("REST request to get BankConnection : {}", id);
        Optional<BankConnectionDTO> bankConnectionDTO = bankConnectionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(bankConnectionDTO);
    }

    /**
     * {@code DELETE  /bank-connections/:id} : delete the "id" bankConnection.
     *
     * @param id the id of the bankConnectionDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBankConnection(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete BankConnection : {}", id);
        bankConnectionService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
