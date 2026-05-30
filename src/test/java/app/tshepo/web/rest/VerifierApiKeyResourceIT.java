package app.tshepo.web.rest;

import static app.tshepo.domain.VerifierApiKeyAsserts.*;
import static app.tshepo.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import app.tshepo.IntegrationTest;
import app.tshepo.domain.VerifierApiKey;
import app.tshepo.repository.VerifierApiKeyRepository;
import app.tshepo.service.dto.VerifierApiKeyDTO;
import app.tshepo.service.mapper.VerifierApiKeyMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link VerifierApiKeyResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class VerifierApiKeyResourceIT {

    private static final String DEFAULT_OWNER_LOGIN = "AAAAAAAAAA";
    private static final String UPDATED_OWNER_LOGIN = "BBBBBBBBBB";

    private static final String DEFAULT_LABEL = "AAAAAAAAAA";
    private static final String UPDATED_LABEL = "BBBBBBBBBB";

    private static final String DEFAULT_KEY_HASH = "AAAAAAAAAA";
    private static final String UPDATED_KEY_HASH = "BBBBBBBBBB";

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final Long DEFAULT_CALL_COUNT = 0L;
    private static final Long UPDATED_CALL_COUNT = 1L;
    private static final Long SMALLER_CALL_COUNT = 0L - 1L;

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/verifier-api-keys";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private VerifierApiKeyRepository verifierApiKeyRepository;

    @Autowired
    private VerifierApiKeyMapper verifierApiKeyMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restVerifierApiKeyMockMvc;

    private VerifierApiKey verifierApiKey;

    private VerifierApiKey insertedVerifierApiKey;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static VerifierApiKey createEntity() {
        return new VerifierApiKey()
            .ownerLogin(DEFAULT_OWNER_LOGIN)
            .label(DEFAULT_LABEL)
            .keyHash(DEFAULT_KEY_HASH)
            .active(DEFAULT_ACTIVE)
            .callCount(DEFAULT_CALL_COUNT)
            .createdAt(DEFAULT_CREATED_AT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static VerifierApiKey createUpdatedEntity() {
        return new VerifierApiKey()
            .ownerLogin(UPDATED_OWNER_LOGIN)
            .label(UPDATED_LABEL)
            .keyHash(UPDATED_KEY_HASH)
            .active(UPDATED_ACTIVE)
            .callCount(UPDATED_CALL_COUNT)
            .createdAt(UPDATED_CREATED_AT);
    }

    @BeforeEach
    void initTest() {
        verifierApiKey = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedVerifierApiKey != null) {
            verifierApiKeyRepository.delete(insertedVerifierApiKey);
            insertedVerifierApiKey = null;
        }
    }

    @Test
    @Transactional
    void createVerifierApiKey() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the VerifierApiKey
        VerifierApiKeyDTO verifierApiKeyDTO = verifierApiKeyMapper.toDto(verifierApiKey);
        var returnedVerifierApiKeyDTO = om.readValue(
            restVerifierApiKeyMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(verifierApiKeyDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            VerifierApiKeyDTO.class
        );

        // Validate the VerifierApiKey in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedVerifierApiKey = verifierApiKeyMapper.toEntity(returnedVerifierApiKeyDTO);
        assertVerifierApiKeyUpdatableFieldsEquals(returnedVerifierApiKey, getPersistedVerifierApiKey(returnedVerifierApiKey));

        insertedVerifierApiKey = returnedVerifierApiKey;
    }

    @Test
    @Transactional
    void createVerifierApiKeyWithExistingId() throws Exception {
        // Create the VerifierApiKey with an existing ID
        verifierApiKey.setId(1L);
        VerifierApiKeyDTO verifierApiKeyDTO = verifierApiKeyMapper.toDto(verifierApiKey);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restVerifierApiKeyMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(verifierApiKeyDTO)))
            .andExpect(status().isBadRequest());

        // Validate the VerifierApiKey in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkOwnerLoginIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        verifierApiKey.setOwnerLogin(null);

        // Create the VerifierApiKey, which fails.
        VerifierApiKeyDTO verifierApiKeyDTO = verifierApiKeyMapper.toDto(verifierApiKey);

        restVerifierApiKeyMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(verifierApiKeyDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkLabelIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        verifierApiKey.setLabel(null);

        // Create the VerifierApiKey, which fails.
        VerifierApiKeyDTO verifierApiKeyDTO = verifierApiKeyMapper.toDto(verifierApiKey);

        restVerifierApiKeyMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(verifierApiKeyDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkKeyHashIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        verifierApiKey.setKeyHash(null);

        // Create the VerifierApiKey, which fails.
        VerifierApiKeyDTO verifierApiKeyDTO = verifierApiKeyMapper.toDto(verifierApiKey);

        restVerifierApiKeyMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(verifierApiKeyDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkActiveIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        verifierApiKey.setActive(null);

        // Create the VerifierApiKey, which fails.
        VerifierApiKeyDTO verifierApiKeyDTO = verifierApiKeyMapper.toDto(verifierApiKey);

        restVerifierApiKeyMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(verifierApiKeyDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCreatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        verifierApiKey.setCreatedAt(null);

        // Create the VerifierApiKey, which fails.
        VerifierApiKeyDTO verifierApiKeyDTO = verifierApiKeyMapper.toDto(verifierApiKey);

        restVerifierApiKeyMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(verifierApiKeyDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllVerifierApiKeys() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        // Get all the verifierApiKeyList
        restVerifierApiKeyMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(verifierApiKey.getId().intValue())))
            .andExpect(jsonPath("$.[*].ownerLogin").value(hasItem(DEFAULT_OWNER_LOGIN)))
            .andExpect(jsonPath("$.[*].label").value(hasItem(DEFAULT_LABEL)))
            .andExpect(jsonPath("$.[*].keyHash").value(hasItem(DEFAULT_KEY_HASH)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)))
            .andExpect(jsonPath("$.[*].callCount").value(hasItem(DEFAULT_CALL_COUNT.intValue())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())));
    }

    @Test
    @Transactional
    void getVerifierApiKey() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        // Get the verifierApiKey
        restVerifierApiKeyMockMvc
            .perform(get(ENTITY_API_URL_ID, verifierApiKey.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(verifierApiKey.getId().intValue()))
            .andExpect(jsonPath("$.ownerLogin").value(DEFAULT_OWNER_LOGIN))
            .andExpect(jsonPath("$.label").value(DEFAULT_LABEL))
            .andExpect(jsonPath("$.keyHash").value(DEFAULT_KEY_HASH))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE))
            .andExpect(jsonPath("$.callCount").value(DEFAULT_CALL_COUNT.intValue()))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()));
    }

    @Test
    @Transactional
    void getVerifierApiKeysByIdFiltering() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        Long id = verifierApiKey.getId();

        defaultVerifierApiKeyFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultVerifierApiKeyFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultVerifierApiKeyFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllVerifierApiKeysByOwnerLoginIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        // Get all the verifierApiKeyList where ownerLogin equals to
        defaultVerifierApiKeyFiltering("ownerLogin.equals=" + DEFAULT_OWNER_LOGIN, "ownerLogin.equals=" + UPDATED_OWNER_LOGIN);
    }

    @Test
    @Transactional
    void getAllVerifierApiKeysByOwnerLoginIsInShouldWork() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        // Get all the verifierApiKeyList where ownerLogin in
        defaultVerifierApiKeyFiltering(
            "ownerLogin.in=" + DEFAULT_OWNER_LOGIN + "," + UPDATED_OWNER_LOGIN,
            "ownerLogin.in=" + UPDATED_OWNER_LOGIN
        );
    }

    @Test
    @Transactional
    void getAllVerifierApiKeysByOwnerLoginIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        // Get all the verifierApiKeyList where ownerLogin is not null
        defaultVerifierApiKeyFiltering("ownerLogin.specified=true", "ownerLogin.specified=false");
    }

    @Test
    @Transactional
    void getAllVerifierApiKeysByOwnerLoginContainsSomething() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        // Get all the verifierApiKeyList where ownerLogin contains
        defaultVerifierApiKeyFiltering("ownerLogin.contains=" + DEFAULT_OWNER_LOGIN, "ownerLogin.contains=" + UPDATED_OWNER_LOGIN);
    }

    @Test
    @Transactional
    void getAllVerifierApiKeysByOwnerLoginNotContainsSomething() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        // Get all the verifierApiKeyList where ownerLogin does not contain
        defaultVerifierApiKeyFiltering(
            "ownerLogin.doesNotContain=" + UPDATED_OWNER_LOGIN,
            "ownerLogin.doesNotContain=" + DEFAULT_OWNER_LOGIN
        );
    }

    @Test
    @Transactional
    void getAllVerifierApiKeysByLabelIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        // Get all the verifierApiKeyList where label equals to
        defaultVerifierApiKeyFiltering("label.equals=" + DEFAULT_LABEL, "label.equals=" + UPDATED_LABEL);
    }

    @Test
    @Transactional
    void getAllVerifierApiKeysByLabelIsInShouldWork() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        // Get all the verifierApiKeyList where label in
        defaultVerifierApiKeyFiltering("label.in=" + DEFAULT_LABEL + "," + UPDATED_LABEL, "label.in=" + UPDATED_LABEL);
    }

    @Test
    @Transactional
    void getAllVerifierApiKeysByLabelIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        // Get all the verifierApiKeyList where label is not null
        defaultVerifierApiKeyFiltering("label.specified=true", "label.specified=false");
    }

    @Test
    @Transactional
    void getAllVerifierApiKeysByLabelContainsSomething() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        // Get all the verifierApiKeyList where label contains
        defaultVerifierApiKeyFiltering("label.contains=" + DEFAULT_LABEL, "label.contains=" + UPDATED_LABEL);
    }

    @Test
    @Transactional
    void getAllVerifierApiKeysByLabelNotContainsSomething() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        // Get all the verifierApiKeyList where label does not contain
        defaultVerifierApiKeyFiltering("label.doesNotContain=" + UPDATED_LABEL, "label.doesNotContain=" + DEFAULT_LABEL);
    }

    @Test
    @Transactional
    void getAllVerifierApiKeysByKeyHashIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        // Get all the verifierApiKeyList where keyHash equals to
        defaultVerifierApiKeyFiltering("keyHash.equals=" + DEFAULT_KEY_HASH, "keyHash.equals=" + UPDATED_KEY_HASH);
    }

    @Test
    @Transactional
    void getAllVerifierApiKeysByKeyHashIsInShouldWork() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        // Get all the verifierApiKeyList where keyHash in
        defaultVerifierApiKeyFiltering("keyHash.in=" + DEFAULT_KEY_HASH + "," + UPDATED_KEY_HASH, "keyHash.in=" + UPDATED_KEY_HASH);
    }

    @Test
    @Transactional
    void getAllVerifierApiKeysByKeyHashIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        // Get all the verifierApiKeyList where keyHash is not null
        defaultVerifierApiKeyFiltering("keyHash.specified=true", "keyHash.specified=false");
    }

    @Test
    @Transactional
    void getAllVerifierApiKeysByKeyHashContainsSomething() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        // Get all the verifierApiKeyList where keyHash contains
        defaultVerifierApiKeyFiltering("keyHash.contains=" + DEFAULT_KEY_HASH, "keyHash.contains=" + UPDATED_KEY_HASH);
    }

    @Test
    @Transactional
    void getAllVerifierApiKeysByKeyHashNotContainsSomething() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        // Get all the verifierApiKeyList where keyHash does not contain
        defaultVerifierApiKeyFiltering("keyHash.doesNotContain=" + UPDATED_KEY_HASH, "keyHash.doesNotContain=" + DEFAULT_KEY_HASH);
    }

    @Test
    @Transactional
    void getAllVerifierApiKeysByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        // Get all the verifierApiKeyList where active equals to
        defaultVerifierApiKeyFiltering("active.equals=" + DEFAULT_ACTIVE, "active.equals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllVerifierApiKeysByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        // Get all the verifierApiKeyList where active in
        defaultVerifierApiKeyFiltering("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE, "active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllVerifierApiKeysByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        // Get all the verifierApiKeyList where active is not null
        defaultVerifierApiKeyFiltering("active.specified=true", "active.specified=false");
    }

    @Test
    @Transactional
    void getAllVerifierApiKeysByCallCountIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        // Get all the verifierApiKeyList where callCount equals to
        defaultVerifierApiKeyFiltering("callCount.equals=" + DEFAULT_CALL_COUNT, "callCount.equals=" + UPDATED_CALL_COUNT);
    }

    @Test
    @Transactional
    void getAllVerifierApiKeysByCallCountIsInShouldWork() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        // Get all the verifierApiKeyList where callCount in
        defaultVerifierApiKeyFiltering(
            "callCount.in=" + DEFAULT_CALL_COUNT + "," + UPDATED_CALL_COUNT,
            "callCount.in=" + UPDATED_CALL_COUNT
        );
    }

    @Test
    @Transactional
    void getAllVerifierApiKeysByCallCountIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        // Get all the verifierApiKeyList where callCount is not null
        defaultVerifierApiKeyFiltering("callCount.specified=true", "callCount.specified=false");
    }

    @Test
    @Transactional
    void getAllVerifierApiKeysByCallCountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        // Get all the verifierApiKeyList where callCount is greater than or equal to
        defaultVerifierApiKeyFiltering(
            "callCount.greaterThanOrEqual=" + DEFAULT_CALL_COUNT,
            "callCount.greaterThanOrEqual=" + UPDATED_CALL_COUNT
        );
    }

    @Test
    @Transactional
    void getAllVerifierApiKeysByCallCountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        // Get all the verifierApiKeyList where callCount is less than or equal to
        defaultVerifierApiKeyFiltering(
            "callCount.lessThanOrEqual=" + DEFAULT_CALL_COUNT,
            "callCount.lessThanOrEqual=" + SMALLER_CALL_COUNT
        );
    }

    @Test
    @Transactional
    void getAllVerifierApiKeysByCallCountIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        // Get all the verifierApiKeyList where callCount is less than
        defaultVerifierApiKeyFiltering("callCount.lessThan=" + UPDATED_CALL_COUNT, "callCount.lessThan=" + DEFAULT_CALL_COUNT);
    }

    @Test
    @Transactional
    void getAllVerifierApiKeysByCallCountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        // Get all the verifierApiKeyList where callCount is greater than
        defaultVerifierApiKeyFiltering("callCount.greaterThan=" + SMALLER_CALL_COUNT, "callCount.greaterThan=" + DEFAULT_CALL_COUNT);
    }

    @Test
    @Transactional
    void getAllVerifierApiKeysByCreatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        // Get all the verifierApiKeyList where createdAt equals to
        defaultVerifierApiKeyFiltering("createdAt.equals=" + DEFAULT_CREATED_AT, "createdAt.equals=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllVerifierApiKeysByCreatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        // Get all the verifierApiKeyList where createdAt in
        defaultVerifierApiKeyFiltering(
            "createdAt.in=" + DEFAULT_CREATED_AT + "," + UPDATED_CREATED_AT,
            "createdAt.in=" + UPDATED_CREATED_AT
        );
    }

    @Test
    @Transactional
    void getAllVerifierApiKeysByCreatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        // Get all the verifierApiKeyList where createdAt is not null
        defaultVerifierApiKeyFiltering("createdAt.specified=true", "createdAt.specified=false");
    }

    private void defaultVerifierApiKeyFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultVerifierApiKeyShouldBeFound(shouldBeFound);
        defaultVerifierApiKeyShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultVerifierApiKeyShouldBeFound(String filter) throws Exception {
        restVerifierApiKeyMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(verifierApiKey.getId().intValue())))
            .andExpect(jsonPath("$.[*].ownerLogin").value(hasItem(DEFAULT_OWNER_LOGIN)))
            .andExpect(jsonPath("$.[*].label").value(hasItem(DEFAULT_LABEL)))
            .andExpect(jsonPath("$.[*].keyHash").value(hasItem(DEFAULT_KEY_HASH)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)))
            .andExpect(jsonPath("$.[*].callCount").value(hasItem(DEFAULT_CALL_COUNT.intValue())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())));

        // Check, that the count call also returns 1
        restVerifierApiKeyMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultVerifierApiKeyShouldNotBeFound(String filter) throws Exception {
        restVerifierApiKeyMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restVerifierApiKeyMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingVerifierApiKey() throws Exception {
        // Get the verifierApiKey
        restVerifierApiKeyMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingVerifierApiKey() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the verifierApiKey
        VerifierApiKey updatedVerifierApiKey = verifierApiKeyRepository.findById(verifierApiKey.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedVerifierApiKey are not directly saved in db
        em.detach(updatedVerifierApiKey);
        updatedVerifierApiKey
            .ownerLogin(UPDATED_OWNER_LOGIN)
            .label(UPDATED_LABEL)
            .keyHash(UPDATED_KEY_HASH)
            .active(UPDATED_ACTIVE)
            .callCount(UPDATED_CALL_COUNT)
            .createdAt(UPDATED_CREATED_AT);
        VerifierApiKeyDTO verifierApiKeyDTO = verifierApiKeyMapper.toDto(updatedVerifierApiKey);

        restVerifierApiKeyMockMvc
            .perform(
                put(ENTITY_API_URL_ID, verifierApiKeyDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(verifierApiKeyDTO))
            )
            .andExpect(status().isOk());

        // Validate the VerifierApiKey in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedVerifierApiKeyToMatchAllProperties(updatedVerifierApiKey);
    }

    @Test
    @Transactional
    void putNonExistingVerifierApiKey() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        verifierApiKey.setId(longCount.incrementAndGet());

        // Create the VerifierApiKey
        VerifierApiKeyDTO verifierApiKeyDTO = verifierApiKeyMapper.toDto(verifierApiKey);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restVerifierApiKeyMockMvc
            .perform(
                put(ENTITY_API_URL_ID, verifierApiKeyDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(verifierApiKeyDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the VerifierApiKey in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchVerifierApiKey() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        verifierApiKey.setId(longCount.incrementAndGet());

        // Create the VerifierApiKey
        VerifierApiKeyDTO verifierApiKeyDTO = verifierApiKeyMapper.toDto(verifierApiKey);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVerifierApiKeyMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(verifierApiKeyDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the VerifierApiKey in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamVerifierApiKey() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        verifierApiKey.setId(longCount.incrementAndGet());

        // Create the VerifierApiKey
        VerifierApiKeyDTO verifierApiKeyDTO = verifierApiKeyMapper.toDto(verifierApiKey);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVerifierApiKeyMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(verifierApiKeyDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the VerifierApiKey in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateVerifierApiKeyWithPatch() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the verifierApiKey using partial update
        VerifierApiKey partialUpdatedVerifierApiKey = new VerifierApiKey();
        partialUpdatedVerifierApiKey.setId(verifierApiKey.getId());

        partialUpdatedVerifierApiKey.keyHash(UPDATED_KEY_HASH).callCount(UPDATED_CALL_COUNT);

        restVerifierApiKeyMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedVerifierApiKey.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedVerifierApiKey))
            )
            .andExpect(status().isOk());

        // Validate the VerifierApiKey in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertVerifierApiKeyUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedVerifierApiKey, verifierApiKey),
            getPersistedVerifierApiKey(verifierApiKey)
        );
    }

    @Test
    @Transactional
    void fullUpdateVerifierApiKeyWithPatch() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the verifierApiKey using partial update
        VerifierApiKey partialUpdatedVerifierApiKey = new VerifierApiKey();
        partialUpdatedVerifierApiKey.setId(verifierApiKey.getId());

        partialUpdatedVerifierApiKey
            .ownerLogin(UPDATED_OWNER_LOGIN)
            .label(UPDATED_LABEL)
            .keyHash(UPDATED_KEY_HASH)
            .active(UPDATED_ACTIVE)
            .callCount(UPDATED_CALL_COUNT)
            .createdAt(UPDATED_CREATED_AT);

        restVerifierApiKeyMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedVerifierApiKey.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedVerifierApiKey))
            )
            .andExpect(status().isOk());

        // Validate the VerifierApiKey in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertVerifierApiKeyUpdatableFieldsEquals(partialUpdatedVerifierApiKey, getPersistedVerifierApiKey(partialUpdatedVerifierApiKey));
    }

    @Test
    @Transactional
    void patchNonExistingVerifierApiKey() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        verifierApiKey.setId(longCount.incrementAndGet());

        // Create the VerifierApiKey
        VerifierApiKeyDTO verifierApiKeyDTO = verifierApiKeyMapper.toDto(verifierApiKey);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restVerifierApiKeyMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, verifierApiKeyDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(verifierApiKeyDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the VerifierApiKey in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchVerifierApiKey() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        verifierApiKey.setId(longCount.incrementAndGet());

        // Create the VerifierApiKey
        VerifierApiKeyDTO verifierApiKeyDTO = verifierApiKeyMapper.toDto(verifierApiKey);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVerifierApiKeyMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(verifierApiKeyDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the VerifierApiKey in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamVerifierApiKey() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        verifierApiKey.setId(longCount.incrementAndGet());

        // Create the VerifierApiKey
        VerifierApiKeyDTO verifierApiKeyDTO = verifierApiKeyMapper.toDto(verifierApiKey);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVerifierApiKeyMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(verifierApiKeyDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the VerifierApiKey in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteVerifierApiKey() throws Exception {
        // Initialize the database
        insertedVerifierApiKey = verifierApiKeyRepository.saveAndFlush(verifierApiKey);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the verifierApiKey
        restVerifierApiKeyMockMvc
            .perform(delete(ENTITY_API_URL_ID, verifierApiKey.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return verifierApiKeyRepository.count();
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

    protected VerifierApiKey getPersistedVerifierApiKey(VerifierApiKey verifierApiKey) {
        return verifierApiKeyRepository.findById(verifierApiKey.getId()).orElseThrow();
    }

    protected void assertPersistedVerifierApiKeyToMatchAllProperties(VerifierApiKey expectedVerifierApiKey) {
        assertVerifierApiKeyAllPropertiesEquals(expectedVerifierApiKey, getPersistedVerifierApiKey(expectedVerifierApiKey));
    }

    protected void assertPersistedVerifierApiKeyToMatchUpdatableProperties(VerifierApiKey expectedVerifierApiKey) {
        assertVerifierApiKeyAllUpdatablePropertiesEquals(expectedVerifierApiKey, getPersistedVerifierApiKey(expectedVerifierApiKey));
    }
}
