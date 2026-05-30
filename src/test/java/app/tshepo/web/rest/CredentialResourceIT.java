package app.tshepo.web.rest;

import static app.tshepo.domain.CredentialAsserts.*;
import static app.tshepo.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import app.tshepo.IntegrationTest;
import app.tshepo.domain.Credential;
import app.tshepo.domain.enumeration.CredentialStatus;
import app.tshepo.repository.CredentialRepository;
import app.tshepo.service.dto.CredentialDTO;
import app.tshepo.service.mapper.CredentialMapper;
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
 * Integration tests for the {@link CredentialResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class CredentialResourceIT {

    private static final String DEFAULT_HOLDER_LOGIN = "AAAAAAAAAA";
    private static final String UPDATED_HOLDER_LOGIN = "BBBBBBBBBB";

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_PURPOSE = "AAAAAAAAAA";
    private static final String UPDATED_PURPOSE = "BBBBBBBBBB";

    private static final CredentialStatus DEFAULT_STATUS = CredentialStatus.ACTIVE;
    private static final CredentialStatus UPDATED_STATUS = CredentialStatus.REVOKED;

    private static final Instant DEFAULT_ISSUED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_ISSUED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_EXPIRES_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_EXPIRES_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_ISSUER_DID = "AAAAAAAAAA";
    private static final String UPDATED_ISSUER_DID = "BBBBBBBBBB";

    private static final String DEFAULT_SD_JWT = "AAAAAAAAAA";
    private static final String UPDATED_SD_JWT = "BBBBBBBBBB";

    private static final String DEFAULT_CLAIMS_SUMMARY = "AAAAAAAAAA";
    private static final String UPDATED_CLAIMS_SUMMARY = "BBBBBBBBBB";

    private static final String DEFAULT_VC_REF = "AAAAAAAAAA";
    private static final String UPDATED_VC_REF = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/credentials";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private CredentialMapper credentialMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCredentialMockMvc;

    private Credential credential;

    private Credential insertedCredential;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Credential createEntity() {
        return new Credential()
            .holderLogin(DEFAULT_HOLDER_LOGIN)
            .title(DEFAULT_TITLE)
            .purpose(DEFAULT_PURPOSE)
            .status(DEFAULT_STATUS)
            .issuedAt(DEFAULT_ISSUED_AT)
            .expiresAt(DEFAULT_EXPIRES_AT)
            .issuerDid(DEFAULT_ISSUER_DID)
            .sdJwt(DEFAULT_SD_JWT)
            .claimsSummary(DEFAULT_CLAIMS_SUMMARY)
            .vcRef(DEFAULT_VC_REF);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Credential createUpdatedEntity() {
        return new Credential()
            .holderLogin(UPDATED_HOLDER_LOGIN)
            .title(UPDATED_TITLE)
            .purpose(UPDATED_PURPOSE)
            .status(UPDATED_STATUS)
            .issuedAt(UPDATED_ISSUED_AT)
            .expiresAt(UPDATED_EXPIRES_AT)
            .issuerDid(UPDATED_ISSUER_DID)
            .sdJwt(UPDATED_SD_JWT)
            .claimsSummary(UPDATED_CLAIMS_SUMMARY)
            .vcRef(UPDATED_VC_REF);
    }

    @BeforeEach
    void initTest() {
        credential = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedCredential != null) {
            credentialRepository.delete(insertedCredential);
            insertedCredential = null;
        }
    }

    @Test
    @Transactional
    void createCredential() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Credential
        CredentialDTO credentialDTO = credentialMapper.toDto(credential);
        var returnedCredentialDTO = om.readValue(
            restCredentialMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(credentialDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            CredentialDTO.class
        );

        // Validate the Credential in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedCredential = credentialMapper.toEntity(returnedCredentialDTO);
        assertCredentialUpdatableFieldsEquals(returnedCredential, getPersistedCredential(returnedCredential));

        insertedCredential = returnedCredential;
    }

    @Test
    @Transactional
    void createCredentialWithExistingId() throws Exception {
        // Create the Credential with an existing ID
        credential.setId(1L);
        CredentialDTO credentialDTO = credentialMapper.toDto(credential);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCredentialMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(credentialDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Credential in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkHolderLoginIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        credential.setHolderLogin(null);

        // Create the Credential, which fails.
        CredentialDTO credentialDTO = credentialMapper.toDto(credential);

        restCredentialMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(credentialDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTitleIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        credential.setTitle(null);

        // Create the Credential, which fails.
        CredentialDTO credentialDTO = credentialMapper.toDto(credential);

        restCredentialMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(credentialDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        credential.setStatus(null);

        // Create the Credential, which fails.
        CredentialDTO credentialDTO = credentialMapper.toDto(credential);

        restCredentialMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(credentialDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkIssuedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        credential.setIssuedAt(null);

        // Create the Credential, which fails.
        CredentialDTO credentialDTO = credentialMapper.toDto(credential);

        restCredentialMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(credentialDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkExpiresAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        credential.setExpiresAt(null);

        // Create the Credential, which fails.
        CredentialDTO credentialDTO = credentialMapper.toDto(credential);

        restCredentialMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(credentialDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkIssuerDidIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        credential.setIssuerDid(null);

        // Create the Credential, which fails.
        CredentialDTO credentialDTO = credentialMapper.toDto(credential);

        restCredentialMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(credentialDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllCredentials() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList
        restCredentialMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(credential.getId().intValue())))
            .andExpect(jsonPath("$.[*].holderLogin").value(hasItem(DEFAULT_HOLDER_LOGIN)))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].purpose").value(hasItem(DEFAULT_PURPOSE)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].issuedAt").value(hasItem(DEFAULT_ISSUED_AT.toString())))
            .andExpect(jsonPath("$.[*].expiresAt").value(hasItem(DEFAULT_EXPIRES_AT.toString())))
            .andExpect(jsonPath("$.[*].issuerDid").value(hasItem(DEFAULT_ISSUER_DID)))
            .andExpect(jsonPath("$.[*].sdJwt").value(hasItem(DEFAULT_SD_JWT)))
            .andExpect(jsonPath("$.[*].claimsSummary").value(hasItem(DEFAULT_CLAIMS_SUMMARY)))
            .andExpect(jsonPath("$.[*].vcRef").value(hasItem(DEFAULT_VC_REF)));
    }

    @Test
    @Transactional
    void getCredential() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get the credential
        restCredentialMockMvc
            .perform(get(ENTITY_API_URL_ID, credential.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(credential.getId().intValue()))
            .andExpect(jsonPath("$.holderLogin").value(DEFAULT_HOLDER_LOGIN))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE))
            .andExpect(jsonPath("$.purpose").value(DEFAULT_PURPOSE))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.issuedAt").value(DEFAULT_ISSUED_AT.toString()))
            .andExpect(jsonPath("$.expiresAt").value(DEFAULT_EXPIRES_AT.toString()))
            .andExpect(jsonPath("$.issuerDid").value(DEFAULT_ISSUER_DID))
            .andExpect(jsonPath("$.sdJwt").value(DEFAULT_SD_JWT))
            .andExpect(jsonPath("$.claimsSummary").value(DEFAULT_CLAIMS_SUMMARY))
            .andExpect(jsonPath("$.vcRef").value(DEFAULT_VC_REF));
    }

    @Test
    @Transactional
    void getCredentialsByIdFiltering() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        Long id = credential.getId();

        defaultCredentialFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultCredentialFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultCredentialFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllCredentialsByHolderLoginIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where holderLogin equals to
        defaultCredentialFiltering("holderLogin.equals=" + DEFAULT_HOLDER_LOGIN, "holderLogin.equals=" + UPDATED_HOLDER_LOGIN);
    }

    @Test
    @Transactional
    void getAllCredentialsByHolderLoginIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where holderLogin in
        defaultCredentialFiltering(
            "holderLogin.in=" + DEFAULT_HOLDER_LOGIN + "," + UPDATED_HOLDER_LOGIN,
            "holderLogin.in=" + UPDATED_HOLDER_LOGIN
        );
    }

    @Test
    @Transactional
    void getAllCredentialsByHolderLoginIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where holderLogin is not null
        defaultCredentialFiltering("holderLogin.specified=true", "holderLogin.specified=false");
    }

    @Test
    @Transactional
    void getAllCredentialsByHolderLoginContainsSomething() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where holderLogin contains
        defaultCredentialFiltering("holderLogin.contains=" + DEFAULT_HOLDER_LOGIN, "holderLogin.contains=" + UPDATED_HOLDER_LOGIN);
    }

    @Test
    @Transactional
    void getAllCredentialsByHolderLoginNotContainsSomething() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where holderLogin does not contain
        defaultCredentialFiltering(
            "holderLogin.doesNotContain=" + UPDATED_HOLDER_LOGIN,
            "holderLogin.doesNotContain=" + DEFAULT_HOLDER_LOGIN
        );
    }

    @Test
    @Transactional
    void getAllCredentialsByTitleIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where title equals to
        defaultCredentialFiltering("title.equals=" + DEFAULT_TITLE, "title.equals=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllCredentialsByTitleIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where title in
        defaultCredentialFiltering("title.in=" + DEFAULT_TITLE + "," + UPDATED_TITLE, "title.in=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllCredentialsByTitleIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where title is not null
        defaultCredentialFiltering("title.specified=true", "title.specified=false");
    }

    @Test
    @Transactional
    void getAllCredentialsByTitleContainsSomething() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where title contains
        defaultCredentialFiltering("title.contains=" + DEFAULT_TITLE, "title.contains=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllCredentialsByTitleNotContainsSomething() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where title does not contain
        defaultCredentialFiltering("title.doesNotContain=" + UPDATED_TITLE, "title.doesNotContain=" + DEFAULT_TITLE);
    }

    @Test
    @Transactional
    void getAllCredentialsByPurposeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where purpose equals to
        defaultCredentialFiltering("purpose.equals=" + DEFAULT_PURPOSE, "purpose.equals=" + UPDATED_PURPOSE);
    }

    @Test
    @Transactional
    void getAllCredentialsByPurposeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where purpose in
        defaultCredentialFiltering("purpose.in=" + DEFAULT_PURPOSE + "," + UPDATED_PURPOSE, "purpose.in=" + UPDATED_PURPOSE);
    }

    @Test
    @Transactional
    void getAllCredentialsByPurposeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where purpose is not null
        defaultCredentialFiltering("purpose.specified=true", "purpose.specified=false");
    }

    @Test
    @Transactional
    void getAllCredentialsByPurposeContainsSomething() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where purpose contains
        defaultCredentialFiltering("purpose.contains=" + DEFAULT_PURPOSE, "purpose.contains=" + UPDATED_PURPOSE);
    }

    @Test
    @Transactional
    void getAllCredentialsByPurposeNotContainsSomething() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where purpose does not contain
        defaultCredentialFiltering("purpose.doesNotContain=" + UPDATED_PURPOSE, "purpose.doesNotContain=" + DEFAULT_PURPOSE);
    }

    @Test
    @Transactional
    void getAllCredentialsByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where status equals to
        defaultCredentialFiltering("status.equals=" + DEFAULT_STATUS, "status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllCredentialsByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where status in
        defaultCredentialFiltering("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS, "status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllCredentialsByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where status is not null
        defaultCredentialFiltering("status.specified=true", "status.specified=false");
    }

    @Test
    @Transactional
    void getAllCredentialsByIssuedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where issuedAt equals to
        defaultCredentialFiltering("issuedAt.equals=" + DEFAULT_ISSUED_AT, "issuedAt.equals=" + UPDATED_ISSUED_AT);
    }

    @Test
    @Transactional
    void getAllCredentialsByIssuedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where issuedAt in
        defaultCredentialFiltering("issuedAt.in=" + DEFAULT_ISSUED_AT + "," + UPDATED_ISSUED_AT, "issuedAt.in=" + UPDATED_ISSUED_AT);
    }

    @Test
    @Transactional
    void getAllCredentialsByIssuedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where issuedAt is not null
        defaultCredentialFiltering("issuedAt.specified=true", "issuedAt.specified=false");
    }

    @Test
    @Transactional
    void getAllCredentialsByExpiresAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where expiresAt equals to
        defaultCredentialFiltering("expiresAt.equals=" + DEFAULT_EXPIRES_AT, "expiresAt.equals=" + UPDATED_EXPIRES_AT);
    }

    @Test
    @Transactional
    void getAllCredentialsByExpiresAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where expiresAt in
        defaultCredentialFiltering("expiresAt.in=" + DEFAULT_EXPIRES_AT + "," + UPDATED_EXPIRES_AT, "expiresAt.in=" + UPDATED_EXPIRES_AT);
    }

    @Test
    @Transactional
    void getAllCredentialsByExpiresAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where expiresAt is not null
        defaultCredentialFiltering("expiresAt.specified=true", "expiresAt.specified=false");
    }

    @Test
    @Transactional
    void getAllCredentialsByIssuerDidIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where issuerDid equals to
        defaultCredentialFiltering("issuerDid.equals=" + DEFAULT_ISSUER_DID, "issuerDid.equals=" + UPDATED_ISSUER_DID);
    }

    @Test
    @Transactional
    void getAllCredentialsByIssuerDidIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where issuerDid in
        defaultCredentialFiltering("issuerDid.in=" + DEFAULT_ISSUER_DID + "," + UPDATED_ISSUER_DID, "issuerDid.in=" + UPDATED_ISSUER_DID);
    }

    @Test
    @Transactional
    void getAllCredentialsByIssuerDidIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where issuerDid is not null
        defaultCredentialFiltering("issuerDid.specified=true", "issuerDid.specified=false");
    }

    @Test
    @Transactional
    void getAllCredentialsByIssuerDidContainsSomething() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where issuerDid contains
        defaultCredentialFiltering("issuerDid.contains=" + DEFAULT_ISSUER_DID, "issuerDid.contains=" + UPDATED_ISSUER_DID);
    }

    @Test
    @Transactional
    void getAllCredentialsByIssuerDidNotContainsSomething() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where issuerDid does not contain
        defaultCredentialFiltering("issuerDid.doesNotContain=" + UPDATED_ISSUER_DID, "issuerDid.doesNotContain=" + DEFAULT_ISSUER_DID);
    }

    @Test
    @Transactional
    void getAllCredentialsByClaimsSummaryIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where claimsSummary equals to
        defaultCredentialFiltering("claimsSummary.equals=" + DEFAULT_CLAIMS_SUMMARY, "claimsSummary.equals=" + UPDATED_CLAIMS_SUMMARY);
    }

    @Test
    @Transactional
    void getAllCredentialsByClaimsSummaryIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where claimsSummary in
        defaultCredentialFiltering(
            "claimsSummary.in=" + DEFAULT_CLAIMS_SUMMARY + "," + UPDATED_CLAIMS_SUMMARY,
            "claimsSummary.in=" + UPDATED_CLAIMS_SUMMARY
        );
    }

    @Test
    @Transactional
    void getAllCredentialsByClaimsSummaryIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where claimsSummary is not null
        defaultCredentialFiltering("claimsSummary.specified=true", "claimsSummary.specified=false");
    }

    @Test
    @Transactional
    void getAllCredentialsByClaimsSummaryContainsSomething() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where claimsSummary contains
        defaultCredentialFiltering("claimsSummary.contains=" + DEFAULT_CLAIMS_SUMMARY, "claimsSummary.contains=" + UPDATED_CLAIMS_SUMMARY);
    }

    @Test
    @Transactional
    void getAllCredentialsByClaimsSummaryNotContainsSomething() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where claimsSummary does not contain
        defaultCredentialFiltering(
            "claimsSummary.doesNotContain=" + UPDATED_CLAIMS_SUMMARY,
            "claimsSummary.doesNotContain=" + DEFAULT_CLAIMS_SUMMARY
        );
    }

    @Test
    @Transactional
    void getAllCredentialsByVcRefIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where vcRef equals to
        defaultCredentialFiltering("vcRef.equals=" + DEFAULT_VC_REF, "vcRef.equals=" + UPDATED_VC_REF);
    }

    @Test
    @Transactional
    void getAllCredentialsByVcRefIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where vcRef in
        defaultCredentialFiltering("vcRef.in=" + DEFAULT_VC_REF + "," + UPDATED_VC_REF, "vcRef.in=" + UPDATED_VC_REF);
    }

    @Test
    @Transactional
    void getAllCredentialsByVcRefIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where vcRef is not null
        defaultCredentialFiltering("vcRef.specified=true", "vcRef.specified=false");
    }

    @Test
    @Transactional
    void getAllCredentialsByVcRefContainsSomething() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where vcRef contains
        defaultCredentialFiltering("vcRef.contains=" + DEFAULT_VC_REF, "vcRef.contains=" + UPDATED_VC_REF);
    }

    @Test
    @Transactional
    void getAllCredentialsByVcRefNotContainsSomething() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        // Get all the credentialList where vcRef does not contain
        defaultCredentialFiltering("vcRef.doesNotContain=" + UPDATED_VC_REF, "vcRef.doesNotContain=" + DEFAULT_VC_REF);
    }

    private void defaultCredentialFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultCredentialShouldBeFound(shouldBeFound);
        defaultCredentialShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultCredentialShouldBeFound(String filter) throws Exception {
        restCredentialMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(credential.getId().intValue())))
            .andExpect(jsonPath("$.[*].holderLogin").value(hasItem(DEFAULT_HOLDER_LOGIN)))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].purpose").value(hasItem(DEFAULT_PURPOSE)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].issuedAt").value(hasItem(DEFAULT_ISSUED_AT.toString())))
            .andExpect(jsonPath("$.[*].expiresAt").value(hasItem(DEFAULT_EXPIRES_AT.toString())))
            .andExpect(jsonPath("$.[*].issuerDid").value(hasItem(DEFAULT_ISSUER_DID)))
            .andExpect(jsonPath("$.[*].sdJwt").value(hasItem(DEFAULT_SD_JWT)))
            .andExpect(jsonPath("$.[*].claimsSummary").value(hasItem(DEFAULT_CLAIMS_SUMMARY)))
            .andExpect(jsonPath("$.[*].vcRef").value(hasItem(DEFAULT_VC_REF)));

        // Check, that the count call also returns 1
        restCredentialMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultCredentialShouldNotBeFound(String filter) throws Exception {
        restCredentialMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCredentialMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingCredential() throws Exception {
        // Get the credential
        restCredentialMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingCredential() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the credential
        Credential updatedCredential = credentialRepository.findById(credential.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedCredential are not directly saved in db
        em.detach(updatedCredential);
        updatedCredential
            .holderLogin(UPDATED_HOLDER_LOGIN)
            .title(UPDATED_TITLE)
            .purpose(UPDATED_PURPOSE)
            .status(UPDATED_STATUS)
            .issuedAt(UPDATED_ISSUED_AT)
            .expiresAt(UPDATED_EXPIRES_AT)
            .issuerDid(UPDATED_ISSUER_DID)
            .sdJwt(UPDATED_SD_JWT)
            .claimsSummary(UPDATED_CLAIMS_SUMMARY)
            .vcRef(UPDATED_VC_REF);
        CredentialDTO credentialDTO = credentialMapper.toDto(updatedCredential);

        restCredentialMockMvc
            .perform(
                put(ENTITY_API_URL_ID, credentialDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(credentialDTO))
            )
            .andExpect(status().isOk());

        // Validate the Credential in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedCredentialToMatchAllProperties(updatedCredential);
    }

    @Test
    @Transactional
    void putNonExistingCredential() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        credential.setId(longCount.incrementAndGet());

        // Create the Credential
        CredentialDTO credentialDTO = credentialMapper.toDto(credential);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCredentialMockMvc
            .perform(
                put(ENTITY_API_URL_ID, credentialDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(credentialDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Credential in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCredential() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        credential.setId(longCount.incrementAndGet());

        // Create the Credential
        CredentialDTO credentialDTO = credentialMapper.toDto(credential);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCredentialMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(credentialDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Credential in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCredential() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        credential.setId(longCount.incrementAndGet());

        // Create the Credential
        CredentialDTO credentialDTO = credentialMapper.toDto(credential);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCredentialMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(credentialDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Credential in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCredentialWithPatch() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the credential using partial update
        Credential partialUpdatedCredential = new Credential();
        partialUpdatedCredential.setId(credential.getId());

        partialUpdatedCredential
            .status(UPDATED_STATUS)
            .expiresAt(UPDATED_EXPIRES_AT)
            .issuerDid(UPDATED_ISSUER_DID)
            .claimsSummary(UPDATED_CLAIMS_SUMMARY)
            .vcRef(UPDATED_VC_REF);

        restCredentialMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCredential.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCredential))
            )
            .andExpect(status().isOk());

        // Validate the Credential in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCredentialUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedCredential, credential),
            getPersistedCredential(credential)
        );
    }

    @Test
    @Transactional
    void fullUpdateCredentialWithPatch() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the credential using partial update
        Credential partialUpdatedCredential = new Credential();
        partialUpdatedCredential.setId(credential.getId());

        partialUpdatedCredential
            .holderLogin(UPDATED_HOLDER_LOGIN)
            .title(UPDATED_TITLE)
            .purpose(UPDATED_PURPOSE)
            .status(UPDATED_STATUS)
            .issuedAt(UPDATED_ISSUED_AT)
            .expiresAt(UPDATED_EXPIRES_AT)
            .issuerDid(UPDATED_ISSUER_DID)
            .sdJwt(UPDATED_SD_JWT)
            .claimsSummary(UPDATED_CLAIMS_SUMMARY)
            .vcRef(UPDATED_VC_REF);

        restCredentialMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCredential.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCredential))
            )
            .andExpect(status().isOk());

        // Validate the Credential in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCredentialUpdatableFieldsEquals(partialUpdatedCredential, getPersistedCredential(partialUpdatedCredential));
    }

    @Test
    @Transactional
    void patchNonExistingCredential() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        credential.setId(longCount.incrementAndGet());

        // Create the Credential
        CredentialDTO credentialDTO = credentialMapper.toDto(credential);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCredentialMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, credentialDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(credentialDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Credential in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCredential() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        credential.setId(longCount.incrementAndGet());

        // Create the Credential
        CredentialDTO credentialDTO = credentialMapper.toDto(credential);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCredentialMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(credentialDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Credential in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCredential() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        credential.setId(longCount.incrementAndGet());

        // Create the Credential
        CredentialDTO credentialDTO = credentialMapper.toDto(credential);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCredentialMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(credentialDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Credential in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCredential() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.saveAndFlush(credential);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the credential
        restCredentialMockMvc
            .perform(delete(ENTITY_API_URL_ID, credential.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return credentialRepository.count();
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

    protected Credential getPersistedCredential(Credential credential) {
        return credentialRepository.findById(credential.getId()).orElseThrow();
    }

    protected void assertPersistedCredentialToMatchAllProperties(Credential expectedCredential) {
        assertCredentialAllPropertiesEquals(expectedCredential, getPersistedCredential(expectedCredential));
    }

    protected void assertPersistedCredentialToMatchUpdatableProperties(Credential expectedCredential) {
        assertCredentialAllUpdatablePropertiesEquals(expectedCredential, getPersistedCredential(expectedCredential));
    }
}
