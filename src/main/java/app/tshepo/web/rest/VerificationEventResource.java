package app.tshepo.web.rest;

import app.tshepo.repository.VerificationEventRepository;
import app.tshepo.service.VerificationEventQueryService;
import app.tshepo.service.VerificationEventService;
import app.tshepo.service.criteria.VerificationEventCriteria;
import app.tshepo.service.dto.VerificationEventDTO;
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
 * REST controller for managing {@link app.tshepo.domain.VerificationEvent}.
 */
@RestController
@RequestMapping("/api/verification-events")
public class VerificationEventResource {

    private static final Logger LOG = LoggerFactory.getLogger(VerificationEventResource.class);

    private static final String ENTITY_NAME = "verificationEvent";

    @Value("${jhipster.clientApp.name:tshepoVault}")
    private String applicationName;

    private final VerificationEventService verificationEventService;

    private final VerificationEventRepository verificationEventRepository;

    private final VerificationEventQueryService verificationEventQueryService;

    public VerificationEventResource(
        VerificationEventService verificationEventService,
        VerificationEventRepository verificationEventRepository,
        VerificationEventQueryService verificationEventQueryService
    ) {
        this.verificationEventService = verificationEventService;
        this.verificationEventRepository = verificationEventRepository;
        this.verificationEventQueryService = verificationEventQueryService;
    }

    /**
     * {@code POST  /verification-events} : Create a new verificationEvent.
     *
     * @param verificationEventDTO the verificationEventDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new verificationEventDTO, or with status {@code 400 (Bad Request)} if the verificationEvent has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<VerificationEventDTO> createVerificationEvent(@Valid @RequestBody VerificationEventDTO verificationEventDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save VerificationEvent : {}", verificationEventDTO);
        if (verificationEventDTO.getId() != null) {
            throw new BadRequestAlertException("A new verificationEvent cannot already have an ID", ENTITY_NAME, "idexists");
        }
        verificationEventDTO = verificationEventService.save(verificationEventDTO);
        return ResponseEntity.created(new URI("/api/verification-events/" + verificationEventDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, verificationEventDTO.getId().toString()))
            .body(verificationEventDTO);
    }

    /**
     * {@code PUT  /verification-events/:id} : Updates an existing verificationEvent.
     *
     * @param id the id of the verificationEventDTO to save.
     * @param verificationEventDTO the verificationEventDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated verificationEventDTO,
     * or with status {@code 400 (Bad Request)} if the verificationEventDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the verificationEventDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<VerificationEventDTO> updateVerificationEvent(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody VerificationEventDTO verificationEventDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update VerificationEvent : {}, {}", id, verificationEventDTO);
        if (verificationEventDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, verificationEventDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!verificationEventRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        verificationEventDTO = verificationEventService.update(verificationEventDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, verificationEventDTO.getId().toString()))
            .body(verificationEventDTO);
    }

    /**
     * {@code PATCH  /verification-events/:id} : Partial updates given fields of an existing verificationEvent, field will ignore if it is null
     *
     * @param id the id of the verificationEventDTO to save.
     * @param verificationEventDTO the verificationEventDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated verificationEventDTO,
     * or with status {@code 400 (Bad Request)} if the verificationEventDTO is not valid,
     * or with status {@code 404 (Not Found)} if the verificationEventDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the verificationEventDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<VerificationEventDTO> partialUpdateVerificationEvent(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody VerificationEventDTO verificationEventDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update VerificationEvent partially : {}, {}", id, verificationEventDTO);
        if (verificationEventDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, verificationEventDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!verificationEventRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<VerificationEventDTO> result = verificationEventService.partialUpdate(verificationEventDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, verificationEventDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /verification-events} : get all the Verification Events.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Verification Events in body.
     */
    @GetMapping("")
    public ResponseEntity<List<VerificationEventDTO>> getAllVerificationEvents(
        VerificationEventCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get VerificationEvents by criteria: {}", criteria);

        Page<VerificationEventDTO> page = verificationEventQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /verification-events/count} : count all the verificationEvents.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countVerificationEvents(VerificationEventCriteria criteria) {
        LOG.debug("REST request to count VerificationEvents by criteria: {}", criteria);
        return ResponseEntity.ok().body(verificationEventQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /verification-events/:id} : get the "id" verificationEvent.
     *
     * @param id the id of the verificationEventDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the verificationEventDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<VerificationEventDTO> getVerificationEvent(@PathVariable("id") Long id) {
        LOG.debug("REST request to get VerificationEvent : {}", id);
        Optional<VerificationEventDTO> verificationEventDTO = verificationEventService.findOne(id);
        return ResponseUtil.wrapOrNotFound(verificationEventDTO);
    }

    /**
     * {@code DELETE  /verification-events/:id} : delete the "id" verificationEvent.
     *
     * @param id the id of the verificationEventDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVerificationEvent(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete VerificationEvent : {}", id);
        verificationEventService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
