package app.tshepo.web.rest;

import static app.tshepo.domain.IssuedClaimAsserts.*;
import static app.tshepo.web.rest.TestUtil.createUpdateProxyForBean;
import static app.tshepo.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import app.tshepo.IntegrationTest;
import app.tshepo.domain.Credential;
import app.tshepo.domain.IssuedClaim;
import app.tshepo.domain.enumeration.ClaimOperator;
import app.tshepo.domain.enumeration.ClaimType;
import app.tshepo.repository.IssuedClaimRepository;
import app.tshepo.service.IssuedClaimService;
import app.tshepo.service.dto.IssuedClaimDTO;
import app.tshepo.service.mapper.IssuedClaimMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
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
 * Integration tests for the {@link IssuedClaimResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class IssuedClaimResourceIT {

    private static final ClaimType DEFAULT_CLAIM_TYPE = ClaimType.INFLOW;
    private static final ClaimType UPDATED_CLAIM_TYPE = ClaimType.BALANCE;

    private static final ClaimOperator DEFAULT_OPERATOR = ClaimOperator.GTE;
    private static final ClaimOperator UPDATED_OPERATOR = ClaimOperator.LTE;

    private static final BigDecimal DEFAULT_THRESHOLD = new BigDecimal(0);
    private static final BigDecimal UPDATED_THRESHOLD = new BigDecimal(1);

    private static final String DEFAULT_CURRENCY = "AAA";
    private static final String UPDATED_CURRENCY = "BBB";

    private static final Integer DEFAULT_PERIOD_MONTHS = 1;
    private static final Integer UPDATED_PERIOD_MONTHS = 2;

    private static final Boolean DEFAULT_MET = false;
    private static final Boolean UPDATED_MET = true;

    private static final String ENTITY_API_URL = "/api/issued-claims";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private IssuedClaimRepository issuedClaimRepository;

    @Mock
    private IssuedClaimRepository issuedClaimRepositoryMock;

    @Autowired
    private IssuedClaimMapper issuedClaimMapper;

    @Mock
    private IssuedClaimService issuedClaimServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restIssuedClaimMockMvc;

    private IssuedClaim issuedClaim;

    private IssuedClaim insertedIssuedClaim;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static IssuedClaim createEntity(EntityManager em) {
        IssuedClaim issuedClaim = new IssuedClaim()
            .claimType(DEFAULT_CLAIM_TYPE)
            .operator(DEFAULT_OPERATOR)
            .threshold(DEFAULT_THRESHOLD)
            .currency(DEFAULT_CURRENCY)
            .periodMonths(DEFAULT_PERIOD_MONTHS)
            .met(DEFAULT_MET);
        // Add required entity
        Credential credential;
        if (TestUtil.findAll(em, Credential.class).isEmpty()) {
            credential = CredentialResourceIT.createEntity();
            em.persist(credential);
            em.flush();
        } else {
            credential = TestUtil.findAll(em, Credential.class).get(0);
        }
        issuedClaim.setCredential(credential);
        return issuedClaim;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static IssuedClaim createUpdatedEntity(EntityManager em) {
        IssuedClaim updatedIssuedClaim = new IssuedClaim()
            .claimType(UPDATED_CLAIM_TYPE)
            .operator(UPDATED_OPERATOR)
            .threshold(UPDATED_THRESHOLD)
            .currency(UPDATED_CURRENCY)
            .periodMonths(UPDATED_PERIOD_MONTHS)
            .met(UPDATED_MET);
        // Add required entity
        Credential credential;
        if (TestUtil.findAll(em, Credential.class).isEmpty()) {
            credential = CredentialResourceIT.createUpdatedEntity();
            em.persist(credential);
            em.flush();
        } else {
            credential = TestUtil.findAll(em, Credential.class).get(0);
        }
        updatedIssuedClaim.setCredential(credential);
        return updatedIssuedClaim;
    }

    @BeforeEach
    void initTest() {
        issuedClaim = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedIssuedClaim != null) {
            issuedClaimRepository.delete(insertedIssuedClaim);
            insertedIssuedClaim = null;
        }
    }

    @Test
    @Transactional
    void createIssuedClaim() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the IssuedClaim
        IssuedClaimDTO issuedClaimDTO = issuedClaimMapper.toDto(issuedClaim);
        var returnedIssuedClaimDTO = om.readValue(
            restIssuedClaimMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(issuedClaimDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            IssuedClaimDTO.class
        );

        // Validate the IssuedClaim in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedIssuedClaim = issuedClaimMapper.toEntity(returnedIssuedClaimDTO);
        assertIssuedClaimUpdatableFieldsEquals(returnedIssuedClaim, getPersistedIssuedClaim(returnedIssuedClaim));

        insertedIssuedClaim = returnedIssuedClaim;
    }

    @Test
    @Transactional
    void createIssuedClaimWithExistingId() throws Exception {
        // Create the IssuedClaim with an existing ID
        issuedClaim.setId(1L);
        IssuedClaimDTO issuedClaimDTO = issuedClaimMapper.toDto(issuedClaim);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restIssuedClaimMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(issuedClaimDTO)))
            .andExpect(status().isBadRequest());

        // Validate the IssuedClaim in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkClaimTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        issuedClaim.setClaimType(null);

        // Create the IssuedClaim, which fails.
        IssuedClaimDTO issuedClaimDTO = issuedClaimMapper.toDto(issuedClaim);

        restIssuedClaimMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(issuedClaimDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkOperatorIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        issuedClaim.setOperator(null);

        // Create the IssuedClaim, which fails.
        IssuedClaimDTO issuedClaimDTO = issuedClaimMapper.toDto(issuedClaim);

        restIssuedClaimMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(issuedClaimDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkThresholdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        issuedClaim.setThreshold(null);

        // Create the IssuedClaim, which fails.
        IssuedClaimDTO issuedClaimDTO = issuedClaimMapper.toDto(issuedClaim);

        restIssuedClaimMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(issuedClaimDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkMetIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        issuedClaim.setMet(null);

        // Create the IssuedClaim, which fails.
        IssuedClaimDTO issuedClaimDTO = issuedClaimMapper.toDto(issuedClaim);

        restIssuedClaimMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(issuedClaimDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllIssuedClaims() throws Exception {
        // Initialize the database
        insertedIssuedClaim = issuedClaimRepository.saveAndFlush(issuedClaim);

        // Get all the issuedClaimList
        restIssuedClaimMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(issuedClaim.getId().intValue())))
            .andExpect(jsonPath("$.[*].claimType").value(hasItem(DEFAULT_CLAIM_TYPE.toString())))
            .andExpect(jsonPath("$.[*].operator").value(hasItem(DEFAULT_OPERATOR.toString())))
            .andExpect(jsonPath("$.[*].threshold").value(hasItem(sameNumber(DEFAULT_THRESHOLD))))
            .andExpect(jsonPath("$.[*].currency").value(hasItem(DEFAULT_CURRENCY)))
            .andExpect(jsonPath("$.[*].periodMonths").value(hasItem(DEFAULT_PERIOD_MONTHS)))
            .andExpect(jsonPath("$.[*].met").value(hasItem(DEFAULT_MET)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllIssuedClaimsWithEagerRelationshipsIsEnabled() throws Exception {
        when(issuedClaimServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restIssuedClaimMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(issuedClaimServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllIssuedClaimsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(issuedClaimServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restIssuedClaimMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(issuedClaimRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getIssuedClaim() throws Exception {
        // Initialize the database
        insertedIssuedClaim = issuedClaimRepository.saveAndFlush(issuedClaim);

        // Get the issuedClaim
        restIssuedClaimMockMvc
            .perform(get(ENTITY_API_URL_ID, issuedClaim.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(issuedClaim.getId().intValue()))
            .andExpect(jsonPath("$.claimType").value(DEFAULT_CLAIM_TYPE.toString()))
            .andExpect(jsonPath("$.operator").value(DEFAULT_OPERATOR.toString()))
            .andExpect(jsonPath("$.threshold").value(sameNumber(DEFAULT_THRESHOLD)))
            .andExpect(jsonPath("$.currency").value(DEFAULT_CURRENCY))
            .andExpect(jsonPath("$.periodMonths").value(DEFAULT_PERIOD_MONTHS))
            .andExpect(jsonPath("$.met").value(DEFAULT_MET));
    }

    @Test
    @Transactional
    void getNonExistingIssuedClaim() throws Exception {
        // Get the issuedClaim
        restIssuedClaimMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingIssuedClaim() throws Exception {
        // Initialize the database
        insertedIssuedClaim = issuedClaimRepository.saveAndFlush(issuedClaim);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the issuedClaim
        IssuedClaim updatedIssuedClaim = issuedClaimRepository.findById(issuedClaim.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedIssuedClaim are not directly saved in db
        em.detach(updatedIssuedClaim);
        updatedIssuedClaim
            .claimType(UPDATED_CLAIM_TYPE)
            .operator(UPDATED_OPERATOR)
            .threshold(UPDATED_THRESHOLD)
            .currency(UPDATED_CURRENCY)
            .periodMonths(UPDATED_PERIOD_MONTHS)
            .met(UPDATED_MET);
        IssuedClaimDTO issuedClaimDTO = issuedClaimMapper.toDto(updatedIssuedClaim);

        restIssuedClaimMockMvc
            .perform(
                put(ENTITY_API_URL_ID, issuedClaimDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(issuedClaimDTO))
            )
            .andExpect(status().isOk());

        // Validate the IssuedClaim in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedIssuedClaimToMatchAllProperties(updatedIssuedClaim);
    }

    @Test
    @Transactional
    void putNonExistingIssuedClaim() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        issuedClaim.setId(longCount.incrementAndGet());

        // Create the IssuedClaim
        IssuedClaimDTO issuedClaimDTO = issuedClaimMapper.toDto(issuedClaim);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restIssuedClaimMockMvc
            .perform(
                put(ENTITY_API_URL_ID, issuedClaimDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(issuedClaimDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the IssuedClaim in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchIssuedClaim() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        issuedClaim.setId(longCount.incrementAndGet());

        // Create the IssuedClaim
        IssuedClaimDTO issuedClaimDTO = issuedClaimMapper.toDto(issuedClaim);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restIssuedClaimMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(issuedClaimDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the IssuedClaim in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamIssuedClaim() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        issuedClaim.setId(longCount.incrementAndGet());

        // Create the IssuedClaim
        IssuedClaimDTO issuedClaimDTO = issuedClaimMapper.toDto(issuedClaim);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restIssuedClaimMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(issuedClaimDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the IssuedClaim in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateIssuedClaimWithPatch() throws Exception {
        // Initialize the database
        insertedIssuedClaim = issuedClaimRepository.saveAndFlush(issuedClaim);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the issuedClaim using partial update
        IssuedClaim partialUpdatedIssuedClaim = new IssuedClaim();
        partialUpdatedIssuedClaim.setId(issuedClaim.getId());

        partialUpdatedIssuedClaim.operator(UPDATED_OPERATOR).threshold(UPDATED_THRESHOLD).currency(UPDATED_CURRENCY);

        restIssuedClaimMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedIssuedClaim.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedIssuedClaim))
            )
            .andExpect(status().isOk());

        // Validate the IssuedClaim in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertIssuedClaimUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedIssuedClaim, issuedClaim),
            getPersistedIssuedClaim(issuedClaim)
        );
    }

    @Test
    @Transactional
    void fullUpdateIssuedClaimWithPatch() throws Exception {
        // Initialize the database
        insertedIssuedClaim = issuedClaimRepository.saveAndFlush(issuedClaim);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the issuedClaim using partial update
        IssuedClaim partialUpdatedIssuedClaim = new IssuedClaim();
        partialUpdatedIssuedClaim.setId(issuedClaim.getId());

        partialUpdatedIssuedClaim
            .claimType(UPDATED_CLAIM_TYPE)
            .operator(UPDATED_OPERATOR)
            .threshold(UPDATED_THRESHOLD)
            .currency(UPDATED_CURRENCY)
            .periodMonths(UPDATED_PERIOD_MONTHS)
            .met(UPDATED_MET);

        restIssuedClaimMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedIssuedClaim.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedIssuedClaim))
            )
            .andExpect(status().isOk());

        // Validate the IssuedClaim in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertIssuedClaimUpdatableFieldsEquals(partialUpdatedIssuedClaim, getPersistedIssuedClaim(partialUpdatedIssuedClaim));
    }

    @Test
    @Transactional
    void patchNonExistingIssuedClaim() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        issuedClaim.setId(longCount.incrementAndGet());

        // Create the IssuedClaim
        IssuedClaimDTO issuedClaimDTO = issuedClaimMapper.toDto(issuedClaim);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restIssuedClaimMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, issuedClaimDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(issuedClaimDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the IssuedClaim in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchIssuedClaim() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        issuedClaim.setId(longCount.incrementAndGet());

        // Create the IssuedClaim
        IssuedClaimDTO issuedClaimDTO = issuedClaimMapper.toDto(issuedClaim);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restIssuedClaimMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(issuedClaimDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the IssuedClaim in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamIssuedClaim() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        issuedClaim.setId(longCount.incrementAndGet());

        // Create the IssuedClaim
        IssuedClaimDTO issuedClaimDTO = issuedClaimMapper.toDto(issuedClaim);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restIssuedClaimMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(issuedClaimDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the IssuedClaim in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteIssuedClaim() throws Exception {
        // Initialize the database
        insertedIssuedClaim = issuedClaimRepository.saveAndFlush(issuedClaim);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the issuedClaim
        restIssuedClaimMockMvc
            .perform(delete(ENTITY_API_URL_ID, issuedClaim.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return issuedClaimRepository.count();
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

    protected IssuedClaim getPersistedIssuedClaim(IssuedClaim issuedClaim) {
        return issuedClaimRepository.findById(issuedClaim.getId()).orElseThrow();
    }

    protected void assertPersistedIssuedClaimToMatchAllProperties(IssuedClaim expectedIssuedClaim) {
        assertIssuedClaimAllPropertiesEquals(expectedIssuedClaim, getPersistedIssuedClaim(expectedIssuedClaim));
    }

    protected void assertPersistedIssuedClaimToMatchUpdatableProperties(IssuedClaim expectedIssuedClaim) {
        assertIssuedClaimAllUpdatablePropertiesEquals(expectedIssuedClaim, getPersistedIssuedClaim(expectedIssuedClaim));
    }
}
