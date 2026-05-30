package app.tshepo.web.rest;

import app.tshepo.repository.CredentialRepository;
import app.tshepo.service.CredentialQueryService;
import app.tshepo.service.CredentialService;
import app.tshepo.service.criteria.CredentialCriteria;
import app.tshepo.service.dto.CredentialDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link app.tshepo.domain.Credential}.
 */
@RestController
@RequestMapping("/api/credentials")
public class CredentialResource {

    private static final Logger LOG = LoggerFactory.getLogger(CredentialResource.class);

    private static final String ENTITY_NAME = "credential";

    @Value("${jhipster.clientApp.name:tshepoVault}")
    private String applicationName;

    private final CredentialService credentialService;

    private final CredentialRepository credentialRepository;

    private final CredentialQueryService credentialQueryService;

    public CredentialResource(
        CredentialService credentialService,
        CredentialRepository credentialRepository,
        CredentialQueryService credentialQueryService
    ) {
        this.credentialService = credentialService;
        this.credentialRepository = credentialRepository;
        this.credentialQueryService = credentialQueryService;
    }

    /**
     * {@code POST  /credentials} : Create a new credential.
     *
     * @param credentialDTO the credentialDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new credentialDTO, or with status {@code 400 (Bad Request)} if the credential has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<CredentialDTO> createCredential(@Valid @RequestBody CredentialDTO credentialDTO) throws URISyntaxException {
        LOG.debug("REST request to save Credential : {}", credentialDTO);
        if (credentialDTO.getId() != null) {
            throw new BadRequestAlertException("A new credential cannot already have an ID", ENTITY_NAME, "idexists");
        }
        credentialDTO = credentialService.save(credentialDTO);
        return ResponseEntity.created(new URI("/api/credentials/" + credentialDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, credentialDTO.getId().toString()))
            .body(credentialDTO);
    }

    /**
     * {@code PUT  /credentials/:id} : Updates an existing credential.
     *
     * @param id the id of the credentialDTO to save.
     * @param credentialDTO the credentialDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated credentialDTO,
     * or with status {@code 400 (Bad Request)} if the credentialDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the credentialDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CredentialDTO> updateCredential(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody CredentialDTO credentialDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Credential : {}, {}", id, credentialDTO);
        if (credentialDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, credentialDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!credentialRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        credentialDTO = credentialService.update(credentialDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, credentialDTO.getId().toString()))
            .body(credentialDTO);
    }

    /**
     * {@code PATCH  /credentials/:id} : Partial updates given fields of an existing credential, field will ignore if it is null
     *
     * @param id the id of the credentialDTO to save.
     * @param credentialDTO the credentialDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated credentialDTO,
     * or with status {@code 400 (Bad Request)} if the credentialDTO is not valid,
     * or with status {@code 404 (Not Found)} if the credentialDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the credentialDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<CredentialDTO> partialUpdateCredential(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody CredentialDTO credentialDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Credential partially : {}, {}", id, credentialDTO);
        if (credentialDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, credentialDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!credentialRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<CredentialDTO> result = credentialService.partialUpdate(credentialDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, credentialDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /credentials} : get all the Credentials.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Credentials in body.
     */
    @GetMapping("")
    public ResponseEntity<List<CredentialDTO>> getAllCredentials(
        CredentialCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Credentials by criteria: {}", criteria);

        Page<CredentialDTO> page = credentialQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /credentials/count} : count all the credentials.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countCredentials(CredentialCriteria criteria) {
        LOG.debug("REST request to count Credentials by criteria: {}", criteria);
        return ResponseEntity.ok().body(credentialQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /credentials/:id} : get the "id" credential.
     *
     * @param id the id of the credentialDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the credentialDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CredentialDTO> getCredential(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Credential : {}", id);
        Optional<CredentialDTO> credentialDTO = credentialService.findOne(id);
        return ResponseUtil.wrapOrNotFound(credentialDTO);
    }

    /**
     * {@code DELETE  /credentials/:id} : delete the "id" credential.
     *
     * @param id the id of the credentialDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCredential(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Credential : {}", id);
        credentialService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
