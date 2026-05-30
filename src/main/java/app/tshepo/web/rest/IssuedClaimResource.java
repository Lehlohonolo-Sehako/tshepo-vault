package app.tshepo.web.rest;

import app.tshepo.repository.IssuedClaimRepository;
import app.tshepo.service.IssuedClaimService;
import app.tshepo.service.dto.IssuedClaimDTO;
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
 * REST controller for managing {@link app.tshepo.domain.IssuedClaim}.
 */
@RestController
@RequestMapping("/api/issued-claims")
public class IssuedClaimResource {

    private static final Logger LOG = LoggerFactory.getLogger(IssuedClaimResource.class);

    private static final String ENTITY_NAME = "issuedClaim";

    @Value("${jhipster.clientApp.name:tshepoVault}")
    private String applicationName;

    private final IssuedClaimService issuedClaimService;

    private final IssuedClaimRepository issuedClaimRepository;

    public IssuedClaimResource(IssuedClaimService issuedClaimService, IssuedClaimRepository issuedClaimRepository) {
        this.issuedClaimService = issuedClaimService;
        this.issuedClaimRepository = issuedClaimRepository;
    }

    /**
     * {@code POST  /issued-claims} : Create a new issuedClaim.
     *
     * @param issuedClaimDTO the issuedClaimDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new issuedClaimDTO, or with status {@code 400 (Bad Request)} if the issuedClaim has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<IssuedClaimDTO> createIssuedClaim(@Valid @RequestBody IssuedClaimDTO issuedClaimDTO) throws URISyntaxException {
        LOG.debug("REST request to save IssuedClaim : {}", issuedClaimDTO);
        if (issuedClaimDTO.getId() != null) {
            throw new BadRequestAlertException("A new issuedClaim cannot already have an ID", ENTITY_NAME, "idexists");
        }
        issuedClaimDTO = issuedClaimService.save(issuedClaimDTO);
        return ResponseEntity.created(new URI("/api/issued-claims/" + issuedClaimDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, issuedClaimDTO.getId().toString()))
            .body(issuedClaimDTO);
    }

    /**
     * {@code PUT  /issued-claims/:id} : Updates an existing issuedClaim.
     *
     * @param id the id of the issuedClaimDTO to save.
     * @param issuedClaimDTO the issuedClaimDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated issuedClaimDTO,
     * or with status {@code 400 (Bad Request)} if the issuedClaimDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the issuedClaimDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<IssuedClaimDTO> updateIssuedClaim(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody IssuedClaimDTO issuedClaimDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update IssuedClaim : {}, {}", id, issuedClaimDTO);
        if (issuedClaimDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, issuedClaimDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!issuedClaimRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        issuedClaimDTO = issuedClaimService.update(issuedClaimDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, issuedClaimDTO.getId().toString()))
            .body(issuedClaimDTO);
    }

    /**
     * {@code PATCH  /issued-claims/:id} : Partial updates given fields of an existing issuedClaim, field will ignore if it is null
     *
     * @param id the id of the issuedClaimDTO to save.
     * @param issuedClaimDTO the issuedClaimDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated issuedClaimDTO,
     * or with status {@code 400 (Bad Request)} if the issuedClaimDTO is not valid,
     * or with status {@code 404 (Not Found)} if the issuedClaimDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the issuedClaimDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<IssuedClaimDTO> partialUpdateIssuedClaim(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody IssuedClaimDTO issuedClaimDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update IssuedClaim partially : {}, {}", id, issuedClaimDTO);
        if (issuedClaimDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, issuedClaimDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!issuedClaimRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<IssuedClaimDTO> result = issuedClaimService.partialUpdate(issuedClaimDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, issuedClaimDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /issued-claims} : get all the Issued Claims.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Issued Claims in body.
     */
    @GetMapping("")
    public List<IssuedClaimDTO> getAllIssuedClaims(
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get all IssuedClaims");
        return issuedClaimService.findAll();
    }

    /**
     * {@code GET  /issued-claims/:id} : get the "id" issuedClaim.
     *
     * @param id the id of the issuedClaimDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the issuedClaimDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<IssuedClaimDTO> getIssuedClaim(@PathVariable("id") Long id) {
        LOG.debug("REST request to get IssuedClaim : {}", id);
        Optional<IssuedClaimDTO> issuedClaimDTO = issuedClaimService.findOne(id);
        return ResponseUtil.wrapOrNotFound(issuedClaimDTO);
    }

    /**
     * {@code DELETE  /issued-claims/:id} : delete the "id" issuedClaim.
     *
     * @param id the id of the issuedClaimDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIssuedClaim(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete IssuedClaim : {}", id);
        issuedClaimService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
