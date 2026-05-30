package app.tshepo.web.rest;

import app.tshepo.repository.VerifierApiKeyRepository;
import app.tshepo.service.VerifierApiKeyQueryService;
import app.tshepo.service.VerifierApiKeyService;
import app.tshepo.service.criteria.VerifierApiKeyCriteria;
import app.tshepo.service.dto.VerifierApiKeyDTO;
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
 * REST controller for managing {@link app.tshepo.domain.VerifierApiKey}.
 */
@RestController
@RequestMapping("/api/verifier-api-keys")
public class VerifierApiKeyResource {

    private static final Logger LOG = LoggerFactory.getLogger(VerifierApiKeyResource.class);

    private static final String ENTITY_NAME = "verifierApiKey";

    @Value("${jhipster.clientApp.name:tshepoVault}")
    private String applicationName;

    private final VerifierApiKeyService verifierApiKeyService;

    private final VerifierApiKeyRepository verifierApiKeyRepository;

    private final VerifierApiKeyQueryService verifierApiKeyQueryService;

    public VerifierApiKeyResource(
        VerifierApiKeyService verifierApiKeyService,
        VerifierApiKeyRepository verifierApiKeyRepository,
        VerifierApiKeyQueryService verifierApiKeyQueryService
    ) {
        this.verifierApiKeyService = verifierApiKeyService;
        this.verifierApiKeyRepository = verifierApiKeyRepository;
        this.verifierApiKeyQueryService = verifierApiKeyQueryService;
    }

    /**
     * {@code POST  /verifier-api-keys} : Create a new verifierApiKey.
     *
     * @param verifierApiKeyDTO the verifierApiKeyDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new verifierApiKeyDTO, or with status {@code 400 (Bad Request)} if the verifierApiKey has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<VerifierApiKeyDTO> createVerifierApiKey(@Valid @RequestBody VerifierApiKeyDTO verifierApiKeyDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save VerifierApiKey : {}", verifierApiKeyDTO);
        if (verifierApiKeyDTO.getId() != null) {
            throw new BadRequestAlertException("A new verifierApiKey cannot already have an ID", ENTITY_NAME, "idexists");
        }
        verifierApiKeyDTO = verifierApiKeyService.save(verifierApiKeyDTO);
        return ResponseEntity.created(new URI("/api/verifier-api-keys/" + verifierApiKeyDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, verifierApiKeyDTO.getId().toString()))
            .body(verifierApiKeyDTO);
    }

    /**
     * {@code PUT  /verifier-api-keys/:id} : Updates an existing verifierApiKey.
     *
     * @param id the id of the verifierApiKeyDTO to save.
     * @param verifierApiKeyDTO the verifierApiKeyDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated verifierApiKeyDTO,
     * or with status {@code 400 (Bad Request)} if the verifierApiKeyDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the verifierApiKeyDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<VerifierApiKeyDTO> updateVerifierApiKey(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody VerifierApiKeyDTO verifierApiKeyDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update VerifierApiKey : {}, {}", id, verifierApiKeyDTO);
        if (verifierApiKeyDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, verifierApiKeyDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!verifierApiKeyRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        verifierApiKeyDTO = verifierApiKeyService.update(verifierApiKeyDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, verifierApiKeyDTO.getId().toString()))
            .body(verifierApiKeyDTO);
    }

    /**
     * {@code PATCH  /verifier-api-keys/:id} : Partial updates given fields of an existing verifierApiKey, field will ignore if it is null
     *
     * @param id the id of the verifierApiKeyDTO to save.
     * @param verifierApiKeyDTO the verifierApiKeyDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated verifierApiKeyDTO,
     * or with status {@code 400 (Bad Request)} if the verifierApiKeyDTO is not valid,
     * or with status {@code 404 (Not Found)} if the verifierApiKeyDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the verifierApiKeyDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<VerifierApiKeyDTO> partialUpdateVerifierApiKey(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody VerifierApiKeyDTO verifierApiKeyDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update VerifierApiKey partially : {}, {}", id, verifierApiKeyDTO);
        if (verifierApiKeyDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, verifierApiKeyDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!verifierApiKeyRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<VerifierApiKeyDTO> result = verifierApiKeyService.partialUpdate(verifierApiKeyDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, verifierApiKeyDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /verifier-api-keys} : get all the Verifier Api Keys.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Verifier Api Keys in body.
     */
    @GetMapping("")
    public ResponseEntity<List<VerifierApiKeyDTO>> getAllVerifierApiKeys(VerifierApiKeyCriteria criteria) {
        LOG.debug("REST request to get VerifierApiKeys by criteria: {}", criteria);

        List<VerifierApiKeyDTO> entityList = verifierApiKeyQueryService.findByCriteria(criteria);
        return ResponseEntity.ok().body(entityList);
    }

    /**
     * {@code GET  /verifier-api-keys/count} : count all the verifierApiKeys.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countVerifierApiKeys(VerifierApiKeyCriteria criteria) {
        LOG.debug("REST request to count VerifierApiKeys by criteria: {}", criteria);
        return ResponseEntity.ok().body(verifierApiKeyQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /verifier-api-keys/:id} : get the "id" verifierApiKey.
     *
     * @param id the id of the verifierApiKeyDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the verifierApiKeyDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<VerifierApiKeyDTO> getVerifierApiKey(@PathVariable("id") Long id) {
        LOG.debug("REST request to get VerifierApiKey : {}", id);
        Optional<VerifierApiKeyDTO> verifierApiKeyDTO = verifierApiKeyService.findOne(id);
        return ResponseUtil.wrapOrNotFound(verifierApiKeyDTO);
    }

    /**
     * {@code DELETE  /verifier-api-keys/:id} : delete the "id" verifierApiKey.
     *
     * @param id the id of the verifierApiKeyDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVerifierApiKey(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete VerifierApiKey : {}", id);
        verifierApiKeyService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
