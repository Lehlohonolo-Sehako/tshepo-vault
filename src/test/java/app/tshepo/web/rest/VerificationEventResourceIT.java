package app.tshepo.web.rest;

import static app.tshepo.domain.VerificationEventAsserts.*;
import static app.tshepo.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import app.tshepo.IntegrationTest;
import app.tshepo.domain.VerificationEvent;
import app.tshepo.domain.VerifierApiKey;
import app.tshepo.domain.enumeration.VerificationResult;
import app.tshepo.repository.VerificationEventRepository;
import app.tshepo.service.VerificationEventService;
import app.tshepo.service.dto.VerificationEventDTO;
import app.tshepo.service.mapper.VerificationEventMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link VerificationEventResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class VerificationEventResourceIT {

    private static final Instant DEFAULT_VERIFIED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_VERIFIED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final VerificationResult DEFAULT_RESULT = VerificationResult.VALID;
    private static final VerificationResult UPDATED_RESULT = VerificationResult.INVALID_SIGNATURE;

    private static final String DEFAULT_DISCLOSED_CLAIMS = "AAAAAAAAAA";
    private static final String UPDATED_DISCLOSED_CLAIMS = "BBBBBBBBBB";

    private static final String DEFAULT_CREDENTIAL_REF = "AAAAAAAAAA";
    private static final String UPDATED_CREDENTIAL_REF = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/verification-events";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private VerificationEventRepository verificationEventRepository;

    @Mock
    private VerificationEventRepository verificationEventRepositoryMock;

    @Autowired
    private VerificationEventMapper verificationEventMapper;

    @Mock
    private VerificationEventService verificationEventServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restVerificationEventMockMvc;

    private VerificationEvent verificationEvent;

    private VerificationEvent insertedVerificationEvent;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static VerificationEvent createEntity() {
        return new VerificationEvent()
            .verifiedAt(DEFAULT_VERIFIED_AT)
            .result(DEFAULT_RESULT)
            .disclosedClaims(DEFAULT_DISCLOSED_CLAIMS)
            .credentialRef(DEFAULT_CREDENTIAL_REF);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static VerificationEvent createUpdatedEntity() {
        return new VerificationEvent()
            .verifiedAt(UPDATED_VERIFIED_AT)
            .result(UPDATED_RESULT)
            .disclosedClaims(UPDATED_DISCLOSED_CLAIMS)
            .credentialRef(UPDATED_CREDENTIAL_REF);
    }

    @BeforeEach
    void initTest() {
        verificationEvent = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedVerificationEvent != null) {
            verificationEventRepository.delete(insertedVerificationEvent);
            insertedVerificationEvent = null;
        }
    }

    @Test
    @Transactional
    void createVerificationEvent() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the VerificationEvent
        VerificationEventDTO verificationEventDTO = verificationEventMapper.toDto(verificationEvent);
        var returnedVerificationEventDTO = om.readValue(
            restVerificationEventMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(verificationEventDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            VerificationEventDTO.class
        );

        // Validate the VerificationEvent in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedVerificationEvent = verificationEventMapper.toEntity(returnedVerificationEventDTO);
        assertVerificationEventUpdatableFieldsEquals(returnedVerificationEvent, getPersistedVerificationEvent(returnedVerificationEvent));

        insertedVerificationEvent = returnedVerificationEvent;
    }

    @Test
    @Transactional
    void createVerificationEventWithExistingId() throws Exception {
        // Create the VerificationEvent with an existing ID
        verificationEvent.setId(1L);
        VerificationEventDTO verificationEventDTO = verificationEventMapper.toDto(verificationEvent);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restVerificationEventMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(verificationEventDTO)))
            .andExpect(status().isBadRequest());

        // Validate the VerificationEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkVerifiedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        verificationEvent.setVerifiedAt(null);

        // Create the VerificationEvent, which fails.
        VerificationEventDTO verificationEventDTO = verificationEventMapper.toDto(verificationEvent);

        restVerificationEventMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(verificationEventDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkResultIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        verificationEvent.setResult(null);

        // Create the VerificationEvent, which fails.
        VerificationEventDTO verificationEventDTO = verificationEventMapper.toDto(verificationEvent);

        restVerificationEventMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(verificationEventDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllVerificationEvents() throws Exception {
        // Initialize the database
        insertedVerificationEvent = verificationEventRepository.saveAndFlush(verificationEvent);

        // Get all the verificationEventList
        restVerificationEventMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(verificationEvent.getId().intValue())))
            .andExpect(jsonPath("$.[*].verifiedAt").value(hasItem(DEFAULT_VERIFIED_AT.toString())))
            .andExpect(jsonPath("$.[*].result").value(hasItem(DEFAULT_RESULT.toString())))
            .andExpect(jsonPath("$.[*].disclosedClaims").value(hasItem(DEFAULT_DISCLOSED_CLAIMS)))
            .andExpect(jsonPath("$.[*].credentialRef").value(hasItem(DEFAULT_CREDENTIAL_REF)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllVerificationEventsWithEagerRelationshipsIsEnabled() throws Exception {
        when(verificationEventServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restVerificationEventMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(verificationEventServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllVerificationEventsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(verificationEventServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restVerificationEventMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(verificationEventRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getVerificationEvent() throws Exception {
        // Initialize the database
        insertedVerificationEvent = verificationEventRepository.saveAndFlush(verificationEvent);

        // Get the verificationEvent
        restVerificationEventMockMvc
            .perform(get(ENTITY_API_URL_ID, verificationEvent.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(verificationEvent.getId().intValue()))
            .andExpect(jsonPath("$.verifiedAt").value(DEFAULT_VERIFIED_AT.toString()))
            .andExpect(jsonPath("$.result").value(DEFAULT_RESULT.toString()))
            .andExpect(jsonPath("$.disclosedClaims").value(DEFAULT_DISCLOSED_CLAIMS))
            .andExpect(jsonPath("$.credentialRef").value(DEFAULT_CREDENTIAL_REF));
    }

    @Test
    @Transactional
    void getVerificationEventsByIdFiltering() throws Exception {
        // Initialize the database
        insertedVerificationEvent = verificationEventRepository.saveAndFlush(verificationEvent);

        Long id = verificationEvent.getId();

        defaultVerificationEventFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultVerificationEventFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultVerificationEventFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllVerificationEventsByVerifiedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedVerificationEvent = verificationEventRepository.saveAndFlush(verificationEvent);

        // Get all the verificationEventList where verifiedAt equals to
        defaultVerificationEventFiltering("verifiedAt.equals=" + DEFAULT_VERIFIED_AT, "verifiedAt.equals=" + UPDATED_VERIFIED_AT);
    }

    @Test
    @Transactional
    void getAllVerificationEventsByVerifiedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedVerificationEvent = verificationEventRepository.saveAndFlush(verificationEvent);

        // Get all the verificationEventList where verifiedAt in
        defaultVerificationEventFiltering(
            "verifiedAt.in=" + DEFAULT_VERIFIED_AT + "," + UPDATED_VERIFIED_AT,
            "verifiedAt.in=" + UPDATED_VERIFIED_AT
        );
    }

    @Test
    @Transactional
    void getAllVerificationEventsByVerifiedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedVerificationEvent = verificationEventRepository.saveAndFlush(verificationEvent);

        // Get all the verificationEventList where verifiedAt is not null
        defaultVerificationEventFiltering("verifiedAt.specified=true", "verifiedAt.specified=false");
    }

    @Test
    @Transactional
    void getAllVerificationEventsByResultIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedVerificationEvent = verificationEventRepository.saveAndFlush(verificationEvent);

        // Get all the verificationEventList where result equals to
        defaultVerificationEventFiltering("result.equals=" + DEFAULT_RESULT, "result.equals=" + UPDATED_RESULT);
    }

    @Test
    @Transactional
    void getAllVerificationEventsByResultIsInShouldWork() throws Exception {
        // Initialize the database
        insertedVerificationEvent = verificationEventRepository.saveAndFlush(verificationEvent);

        // Get all the verificationEventList where result in
        defaultVerificationEventFiltering("result.in=" + DEFAULT_RESULT + "," + UPDATED_RESULT, "result.in=" + UPDATED_RESULT);
    }

    @Test
    @Transactional
    void getAllVerificationEventsByResultIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedVerificationEvent = verificationEventRepository.saveAndFlush(verificationEvent);

        // Get all the verificationEventList where result is not null
        defaultVerificationEventFiltering("result.specified=true", "result.specified=false");
    }

    @Test
    @Transactional
    void getAllVerificationEventsByCredentialRefIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedVerificationEvent = verificationEventRepository.saveAndFlush(verificationEvent);

        // Get all the verificationEventList where credentialRef equals to
        defaultVerificationEventFiltering(
            "credentialRef.equals=" + DEFAULT_CREDENTIAL_REF,
            "credentialRef.equals=" + UPDATED_CREDENTIAL_REF
        );
    }

    @Test
    @Transactional
    void getAllVerificationEventsByCredentialRefIsInShouldWork() throws Exception {
        // Initialize the database
        insertedVerificationEvent = verificationEventRepository.saveAndFlush(verificationEvent);

        // Get all the verificationEventList where credentialRef in
        defaultVerificationEventFiltering(
            "credentialRef.in=" + DEFAULT_CREDENTIAL_REF + "," + UPDATED_CREDENTIAL_REF,
            "credentialRef.in=" + UPDATED_CREDENTIAL_REF
        );
    }

    @Test
    @Transactional
    void getAllVerificationEventsByCredentialRefIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedVerificationEvent = verificationEventRepository.saveAndFlush(verificationEvent);

        // Get all the verificationEventList where credentialRef is not null
        defaultVerificationEventFiltering("credentialRef.specified=true", "credentialRef.specified=false");
    }

    @Test
    @Transactional
    void getAllVerificationEventsByCredentialRefContainsSomething() throws Exception {
        // Initialize the database
        insertedVerificationEvent = verificationEventRepository.saveAndFlush(verificationEvent);

        // Get all the verificationEventList where credentialRef contains
        defaultVerificationEventFiltering(
            "credentialRef.contains=" + DEFAULT_CREDENTIAL_REF,
            "credentialRef.contains=" + UPDATED_CREDENTIAL_REF
        );
    }

    @Test
    @Transactional
    void getAllVerificationEventsByCredentialRefNotContainsSomething() throws Exception {
        // Initialize the database
        insertedVerificationEvent = verificationEventRepository.saveAndFlush(verificationEvent);

        // Get all the verificationEventList where credentialRef does not contain
        defaultVerificationEventFiltering(
            "credentialRef.doesNotContain=" + UPDATED_CREDENTIAL_REF,
            "credentialRef.doesNotContain=" + DEFAULT_CREDENTIAL_REF
        );
    }

    @Test
    @Transactional
    void getAllVerificationEventsByApiKeyIsEqualToSomething() throws Exception {
        VerifierApiKey apiKey;
        if (TestUtil.findAll(em, VerifierApiKey.class).isEmpty()) {
            verificationEventRepository.saveAndFlush(verificationEvent);
            apiKey = VerifierApiKeyResourceIT.createEntity();
        } else {
            apiKey = TestUtil.findAll(em, VerifierApiKey.class).get(0);
        }
        em.persist(apiKey);
        em.flush();
        verificationEvent.setApiKey(apiKey);
        verificationEventRepository.saveAndFlush(verificationEvent);
        Long apiKeyId = apiKey.getId();
        // Get all the verificationEventList where apiKey equals to apiKeyId
        defaultVerificationEventShouldBeFound("apiKeyId.equals=" + apiKeyId);

        // Get all the verificationEventList where apiKey equals to (apiKeyId + 1)
        defaultVerificationEventShouldNotBeFound("apiKeyId.equals=" + (apiKeyId + 1));
    }

    private void defaultVerificationEventFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultVerificationEventShouldBeFound(shouldBeFound);
        defaultVerificationEventShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultVerificationEventShouldBeFound(String filter) throws Exception {
        restVerificationEventMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(verificationEvent.getId().intValue())))
            .andExpect(jsonPath("$.[*].verifiedAt").value(hasItem(DEFAULT_VERIFIED_AT.toString())))
            .andExpect(jsonPath("$.[*].result").value(hasItem(DEFAULT_RESULT.toString())))
            .andExpect(jsonPath("$.[*].disclosedClaims").value(hasItem(DEFAULT_DISCLOSED_CLAIMS)))
            .andExpect(jsonPath("$.[*].credentialRef").value(hasItem(DEFAULT_CREDENTIAL_REF)));

        // Check, that the count call also returns 1
        restVerificationEventMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultVerificationEventShouldNotBeFound(String filter) throws Exception {
        restVerificationEventMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restVerificationEventMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingVerificationEvent() throws Exception {
        // Get the verificationEvent
        restVerificationEventMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingVerificationEvent() throws Exception {
        // Initialize the database
        insertedVerificationEvent = verificationEventRepository.saveAndFlush(verificationEvent);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the verificationEvent
        VerificationEvent updatedVerificationEvent = verificationEventRepository.findById(verificationEvent.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedVerificationEvent are not directly saved in db
        em.detach(updatedVerificationEvent);
        updatedVerificationEvent
            .verifiedAt(UPDATED_VERIFIED_AT)
            .result(UPDATED_RESULT)
            .disclosedClaims(UPDATED_DISCLOSED_CLAIMS)
            .credentialRef(UPDATED_CREDENTIAL_REF);
        VerificationEventDTO verificationEventDTO = verificationEventMapper.toDto(updatedVerificationEvent);

        restVerificationEventMockMvc
            .perform(
                put(ENTITY_API_URL_ID, verificationEventDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(verificationEventDTO))
            )
            .andExpect(status().isOk());

        // Validate the VerificationEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedVerificationEventToMatchAllProperties(updatedVerificationEvent);
    }

    @Test
    @Transactional
    void putNonExistingVerificationEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        verificationEvent.setId(longCount.incrementAndGet());

        // Create the VerificationEvent
        VerificationEventDTO verificationEventDTO = verificationEventMapper.toDto(verificationEvent);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restVerificationEventMockMvc
            .perform(
                put(ENTITY_API_URL_ID, verificationEventDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(verificationEventDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the VerificationEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchVerificationEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        verificationEvent.setId(longCount.incrementAndGet());

        // Create the VerificationEvent
        VerificationEventDTO verificationEventDTO = verificationEventMapper.toDto(verificationEvent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVerificationEventMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(verificationEventDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the VerificationEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamVerificationEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        verificationEvent.setId(longCount.incrementAndGet());

        // Create the VerificationEvent
        VerificationEventDTO verificationEventDTO = verificationEventMapper.toDto(verificationEvent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVerificationEventMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(verificationEventDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the VerificationEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateVerificationEventWithPatch() throws Exception {
        // Initialize the database
        insertedVerificationEvent = verificationEventRepository.saveAndFlush(verificationEvent);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the verificationEvent using partial update
        VerificationEvent partialUpdatedVerificationEvent = new VerificationEvent();
        partialUpdatedVerificationEvent.setId(verificationEvent.getId());

        partialUpdatedVerificationEvent.verifiedAt(UPDATED_VERIFIED_AT).disclosedClaims(UPDATED_DISCLOSED_CLAIMS);

        restVerificationEventMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedVerificationEvent.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedVerificationEvent))
            )
            .andExpect(status().isOk());

        // Validate the VerificationEvent in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertVerificationEventUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedVerificationEvent, verificationEvent),
            getPersistedVerificationEvent(verificationEvent)
        );
    }

    @Test
    @Transactional
    void fullUpdateVerificationEventWithPatch() throws Exception {
        // Initialize the database
        insertedVerificationEvent = verificationEventRepository.saveAndFlush(verificationEvent);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the verificationEvent using partial update
        VerificationEvent partialUpdatedVerificationEvent = new VerificationEvent();
        partialUpdatedVerificationEvent.setId(verificationEvent.getId());

        partialUpdatedVerificationEvent
            .verifiedAt(UPDATED_VERIFIED_AT)
            .result(UPDATED_RESULT)
            .disclosedClaims(UPDATED_DISCLOSED_CLAIMS)
            .credentialRef(UPDATED_CREDENTIAL_REF);

        restVerificationEventMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedVerificationEvent.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedVerificationEvent))
            )
            .andExpect(status().isOk());

        // Validate the VerificationEvent in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertVerificationEventUpdatableFieldsEquals(
            partialUpdatedVerificationEvent,
            getPersistedVerificationEvent(partialUpdatedVerificationEvent)
        );
    }

    @Test
    @Transactional
    void patchNonExistingVerificationEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        verificationEvent.setId(longCount.incrementAndGet());

        // Create the VerificationEvent
        VerificationEventDTO verificationEventDTO = verificationEventMapper.toDto(verificationEvent);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restVerificationEventMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, verificationEventDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(verificationEventDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the VerificationEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchVerificationEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        verificationEvent.setId(longCount.incrementAndGet());

        // Create the VerificationEvent
        VerificationEventDTO verificationEventDTO = verificationEventMapper.toDto(verificationEvent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVerificationEventMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(verificationEventDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the VerificationEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamVerificationEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        verificationEvent.setId(longCount.incrementAndGet());

        // Create the VerificationEvent
        VerificationEventDTO verificationEventDTO = verificationEventMapper.toDto(verificationEvent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVerificationEventMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(verificationEventDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the VerificationEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteVerificationEvent() throws Exception {
        // Initialize the database
        insertedVerificationEvent = verificationEventRepository.saveAndFlush(verificationEvent);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the verificationEvent
        restVerificationEventMockMvc
            .perform(delete(ENTITY_API_URL_ID, verificationEvent.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return verificationEventRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected VerificationEvent getPersistedVerificationEvent(VerificationEvent verificationEvent) {
        return verificationEventRepository.findById(verificationEvent.getId()).orElseThrow();
    }

    protected void assertPersistedVerificationEventToMatchAllProperties(VerificationEvent expectedVerificationEvent) {
        assertVerificationEventAllPropertiesEquals(expectedVerificationEvent, getPersistedVerificationEvent(expectedVerificationEvent));
    }

    protected void assertPersistedVerificationEventToMatchUpdatableProperties(VerificationEvent expectedVerificationEvent) {
        assertVerificationEventAllUpdatablePropertiesEquals(
            expectedVerificationEvent,
            getPersistedVerificationEvent(expectedVerificationEvent)
        );
    }
}
