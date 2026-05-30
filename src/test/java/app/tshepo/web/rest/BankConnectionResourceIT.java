package app.tshepo.web.rest;

import static app.tshepo.domain.BankConnectionAsserts.*;
import static app.tshepo.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import app.tshepo.IntegrationTest;
import app.tshepo.domain.BankConnection;
import app.tshepo.domain.enumeration.BankConnectionStatus;
import app.tshepo.repository.BankConnectionRepository;
import app.tshepo.service.dto.BankConnectionDTO;
import app.tshepo.service.mapper.BankConnectionMapper;
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
 * Integration tests for the {@link BankConnectionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class BankConnectionResourceIT {

    private static final String DEFAULT_HOLDER_LOGIN = "AAAAAAAAAA";
    private static final String UPDATED_HOLDER_LOGIN = "BBBBBBBBBB";

    private static final Instant DEFAULT_CONNECTED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CONNECTED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final BankConnectionStatus DEFAULT_STATUS = BankConnectionStatus.CONNECTED;
    private static final BankConnectionStatus UPDATED_STATUS = BankConnectionStatus.DISCONNECTED;

    private static final String DEFAULT_MASKED_ACCOUNT = "AAAAAAAAAA";
    private static final String UPDATED_MASKED_ACCOUNT = "BBBBBBBBBB";

    private static final String DEFAULT_ACCOUNT_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_ACCOUNT_TYPE = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/bank-connections";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private BankConnectionRepository bankConnectionRepository;

    @Autowired
    private BankConnectionMapper bankConnectionMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restBankConnectionMockMvc;

    private BankConnection bankConnection;

    private BankConnection insertedBankConnection;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BankConnection createEntity() {
        return new BankConnection()
            .holderLogin(DEFAULT_HOLDER_LOGIN)
            .connectedAt(DEFAULT_CONNECTED_AT)
            .status(DEFAULT_STATUS)
            .maskedAccount(DEFAULT_MASKED_ACCOUNT)
            .accountType(DEFAULT_ACCOUNT_TYPE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BankConnection createUpdatedEntity() {
        return new BankConnection()
            .holderLogin(UPDATED_HOLDER_LOGIN)
            .connectedAt(UPDATED_CONNECTED_AT)
            .status(UPDATED_STATUS)
            .maskedAccount(UPDATED_MASKED_ACCOUNT)
            .accountType(UPDATED_ACCOUNT_TYPE);
    }

    @BeforeEach
    void initTest() {
        bankConnection = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedBankConnection != null) {
            bankConnectionRepository.delete(insertedBankConnection);
            insertedBankConnection = null;
        }
    }

    @Test
    @Transactional
    void createBankConnection() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the BankConnection
        BankConnectionDTO bankConnectionDTO = bankConnectionMapper.toDto(bankConnection);
        var returnedBankConnectionDTO = om.readValue(
            restBankConnectionMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(bankConnectionDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            BankConnectionDTO.class
        );

        // Validate the BankConnection in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedBankConnection = bankConnectionMapper.toEntity(returnedBankConnectionDTO);
        assertBankConnectionUpdatableFieldsEquals(returnedBankConnection, getPersistedBankConnection(returnedBankConnection));

        insertedBankConnection = returnedBankConnection;
    }

    @Test
    @Transactional
    void createBankConnectionWithExistingId() throws Exception {
        // Create the BankConnection with an existing ID
        bankConnection.setId(1L);
        BankConnectionDTO bankConnectionDTO = bankConnectionMapper.toDto(bankConnection);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restBankConnectionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(bankConnectionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the BankConnection in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkHolderLoginIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        bankConnection.setHolderLogin(null);

        // Create the BankConnection, which fails.
        BankConnectionDTO bankConnectionDTO = bankConnectionMapper.toDto(bankConnection);

        restBankConnectionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(bankConnectionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkConnectedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        bankConnection.setConnectedAt(null);

        // Create the BankConnection, which fails.
        BankConnectionDTO bankConnectionDTO = bankConnectionMapper.toDto(bankConnection);

        restBankConnectionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(bankConnectionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        bankConnection.setStatus(null);

        // Create the BankConnection, which fails.
        BankConnectionDTO bankConnectionDTO = bankConnectionMapper.toDto(bankConnection);

        restBankConnectionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(bankConnectionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllBankConnections() throws Exception {
        // Initialize the database
        insertedBankConnection = bankConnectionRepository.saveAndFlush(bankConnection);

        // Get all the bankConnectionList
        restBankConnectionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(bankConnection.getId().intValue())))
            .andExpect(jsonPath("$.[*].holderLogin").value(hasItem(DEFAULT_HOLDER_LOGIN)))
            .andExpect(jsonPath("$.[*].connectedAt").value(hasItem(DEFAULT_CONNECTED_AT.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].maskedAccount").value(hasItem(DEFAULT_MASKED_ACCOUNT)))
            .andExpect(jsonPath("$.[*].accountType").value(hasItem(DEFAULT_ACCOUNT_TYPE)));
    }

    @Test
    @Transactional
    void getBankConnection() throws Exception {
        // Initialize the database
        insertedBankConnection = bankConnectionRepository.saveAndFlush(bankConnection);

        // Get the bankConnection
        restBankConnectionMockMvc
            .perform(get(ENTITY_API_URL_ID, bankConnection.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(bankConnection.getId().intValue()))
            .andExpect(jsonPath("$.holderLogin").value(DEFAULT_HOLDER_LOGIN))
            .andExpect(jsonPath("$.connectedAt").value(DEFAULT_CONNECTED_AT.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.maskedAccount").value(DEFAULT_MASKED_ACCOUNT))
            .andExpect(jsonPath("$.accountType").value(DEFAULT_ACCOUNT_TYPE));
    }

    @Test
    @Transactional
    void getNonExistingBankConnection() throws Exception {
        // Get the bankConnection
        restBankConnectionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingBankConnection() throws Exception {
        // Initialize the database
        insertedBankConnection = bankConnectionRepository.saveAndFlush(bankConnection);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the bankConnection
        BankConnection updatedBankConnection = bankConnectionRepository.findById(bankConnection.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedBankConnection are not directly saved in db
        em.detach(updatedBankConnection);
        updatedBankConnection
            .holderLogin(UPDATED_HOLDER_LOGIN)
            .connectedAt(UPDATED_CONNECTED_AT)
            .status(UPDATED_STATUS)
            .maskedAccount(UPDATED_MASKED_ACCOUNT)
            .accountType(UPDATED_ACCOUNT_TYPE);
        BankConnectionDTO bankConnectionDTO = bankConnectionMapper.toDto(updatedBankConnection);

        restBankConnectionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, bankConnectionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(bankConnectionDTO))
            )
            .andExpect(status().isOk());

        // Validate the BankConnection in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedBankConnectionToMatchAllProperties(updatedBankConnection);
    }

    @Test
    @Transactional
    void putNonExistingBankConnection() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        bankConnection.setId(longCount.incrementAndGet());

        // Create the BankConnection
        BankConnectionDTO bankConnectionDTO = bankConnectionMapper.toDto(bankConnection);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBankConnectionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, bankConnectionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(bankConnectionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BankConnection in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchBankConnection() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        bankConnection.setId(longCount.incrementAndGet());

        // Create the BankConnection
        BankConnectionDTO bankConnectionDTO = bankConnectionMapper.toDto(bankConnection);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBankConnectionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(bankConnectionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BankConnection in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamBankConnection() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        bankConnection.setId(longCount.incrementAndGet());

        // Create the BankConnection
        BankConnectionDTO bankConnectionDTO = bankConnectionMapper.toDto(bankConnection);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBankConnectionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(bankConnectionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the BankConnection in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateBankConnectionWithPatch() throws Exception {
        // Initialize the database
        insertedBankConnection = bankConnectionRepository.saveAndFlush(bankConnection);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the bankConnection using partial update
        BankConnection partialUpdatedBankConnection = new BankConnection();
        partialUpdatedBankConnection.setId(bankConnection.getId());

        partialUpdatedBankConnection.holderLogin(UPDATED_HOLDER_LOGIN).accountType(UPDATED_ACCOUNT_TYPE);

        restBankConnectionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBankConnection.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedBankConnection))
            )
            .andExpect(status().isOk());

        // Validate the BankConnection in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBankConnectionUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedBankConnection, bankConnection),
            getPersistedBankConnection(bankConnection)
        );
    }

    @Test
    @Transactional
    void fullUpdateBankConnectionWithPatch() throws Exception {
        // Initialize the database
        insertedBankConnection = bankConnectionRepository.saveAndFlush(bankConnection);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the bankConnection using partial update
        BankConnection partialUpdatedBankConnection = new BankConnection();
        partialUpdatedBankConnection.setId(bankConnection.getId());

        partialUpdatedBankConnection
            .holderLogin(UPDATED_HOLDER_LOGIN)
            .connectedAt(UPDATED_CONNECTED_AT)
            .status(UPDATED_STATUS)
            .maskedAccount(UPDATED_MASKED_ACCOUNT)
            .accountType(UPDATED_ACCOUNT_TYPE);

        restBankConnectionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBankConnection.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedBankConnection))
            )
            .andExpect(status().isOk());

        // Validate the BankConnection in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBankConnectionUpdatableFieldsEquals(partialUpdatedBankConnection, getPersistedBankConnection(partialUpdatedBankConnection));
    }

    @Test
    @Transactional
    void patchNonExistingBankConnection() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        bankConnection.setId(longCount.incrementAndGet());

        // Create the BankConnection
        BankConnectionDTO bankConnectionDTO = bankConnectionMapper.toDto(bankConnection);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBankConnectionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, bankConnectionDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(bankConnectionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BankConnection in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchBankConnection() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        bankConnection.setId(longCount.incrementAndGet());

        // Create the BankConnection
        BankConnectionDTO bankConnectionDTO = bankConnectionMapper.toDto(bankConnection);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBankConnectionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(bankConnectionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BankConnection in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamBankConnection() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        bankConnection.setId(longCount.incrementAndGet());

        // Create the BankConnection
        BankConnectionDTO bankConnectionDTO = bankConnectionMapper.toDto(bankConnection);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBankConnectionMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(bankConnectionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the BankConnection in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteBankConnection() throws Exception {
        // Initialize the database
        insertedBankConnection = bankConnectionRepository.saveAndFlush(bankConnection);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the bankConnection
        restBankConnectionMockMvc
            .perform(delete(ENTITY_API_URL_ID, bankConnection.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return bankConnectionRepository.count();
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

    protected BankConnection getPersistedBankConnection(BankConnection bankConnection) {
        return bankConnectionRepository.findById(bankConnection.getId()).orElseThrow();
    }

    protected void assertPersistedBankConnectionToMatchAllProperties(BankConnection expectedBankConnection) {
        assertBankConnectionAllPropertiesEquals(expectedBankConnection, getPersistedBankConnection(expectedBankConnection));
    }

    protected void assertPersistedBankConnectionToMatchUpdatableProperties(BankConnection expectedBankConnection) {
        assertBankConnectionAllUpdatablePropertiesEquals(expectedBankConnection, getPersistedBankConnection(expectedBankConnection));
    }
}
