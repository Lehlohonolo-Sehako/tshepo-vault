import {
  entityConfirmDeleteButtonSelector,
  entityCreateButtonSelector,
  entityCreateCancelButtonSelector,
  entityCreateSaveButtonSelector,
  entityDeleteButtonSelector,
  entityDetailsBackButtonSelector,
  entityDetailsButtonSelector,
  entityEditButtonSelector,
  entityTableSelector,
} from '../../support/entity';

describe('IssuedClaim e2e test', () => {
  const issuedClaimPageUrl = '/issued-claim';
  const issuedClaimPageUrlPattern = new RegExp('/issued-claim(\\?.*)?$');
  let username: string;
  let password: string;
  const issuedClaimSample = { claimType: 'BALANCE', operator: 'LTE', threshold: 8694.2, met: false };

  let issuedClaim;
  let credential;

  before(() => {
    cy.credentials().then(credentials => {
      ({ username, password } = credentials);
    });
  });

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    // create an instance at the required relationship entity:
    cy.authenticatedRequest({
      method: 'POST',
      url: '/api/credentials',
      body: {
        holderLogin: 'thump',
        title: 'whose among belabor',
        purpose: 'utterly',
        status: 'REVOKED',
        issuedAt: '2026-05-28T18:25:24.951Z',
        expiresAt: '2026-05-28T23:00:14.194Z',
        issuerDid: 'among',
        sdJwt: 'Li4vZmFrZS1kYXRhL2Jsb2IvaGlwc3Rlci50eHQ=',
        claimsSummary: 'convection',
        vcRef: 'drat',
      },
    }).then(({ body }) => {
      credential = body;
    });
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/issued-claims+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/issued-claims').as('postEntityRequest');
    cy.intercept('DELETE', '/api/issued-claims/*').as('deleteEntityRequest');
  });

  beforeEach(() => {
    // Simulate relationships api for better performance and reproducibility.
    cy.intercept('GET', '/api/credentials', {
      statusCode: 200,
      body: [credential],
    });
  });

  afterEach(() => {
    if (issuedClaim) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/issued-claims/${issuedClaim.id}`,
      }).then(() => {
        issuedClaim = undefined;
      });
    }
  });

  afterEach(() => {
    if (credential) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/credentials/${credential.id}`,
      }).then(() => {
        credential = undefined;
      });
    }
  });

  it('IssuedClaims menu should load IssuedClaims page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('issued-claim');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('IssuedClaim').should('exist');
    cy.url().should('match', issuedClaimPageUrlPattern);
  });

  describe('IssuedClaim page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(issuedClaimPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create IssuedClaim page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/issued-claim/new$'));
        cy.getEntityCreateUpdateHeading('IssuedClaim');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', issuedClaimPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/issued-claims',
          body: {
            ...issuedClaimSample,
            credential,
          },
        }).then(({ body }) => {
          issuedClaim = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/issued-claims+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [issuedClaim],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(issuedClaimPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details IssuedClaim page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('issuedClaim');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', issuedClaimPageUrlPattern);
      });

      it('edit button click should load edit IssuedClaim page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('IssuedClaim');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', issuedClaimPageUrlPattern);
      });

      it('edit button click should load edit IssuedClaim page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('IssuedClaim');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', issuedClaimPageUrlPattern);
      });

      it('last delete button click should delete instance of IssuedClaim', () => {
        cy.intercept('GET', '/api/issued-claims/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('issuedClaim').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', issuedClaimPageUrlPattern);

        issuedClaim = undefined;
      });
    });
  });

  describe('new IssuedClaim page', () => {
    beforeEach(() => {
      cy.visit(issuedClaimPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('IssuedClaim');
    });

    it('should create an instance of IssuedClaim', () => {
      cy.get(`[data-cy="claimType"]`).select('SALARY_CONTINUITY');

      cy.get(`[data-cy="operator"]`).select('LTE');

      cy.get(`[data-cy="threshold"]`).type('675.73');
      cy.get(`[data-cy="threshold"]`).should('have.value', '675.73');

      cy.get(`[data-cy="currency"]`).type('deg');
      cy.get(`[data-cy="currency"]`).should('have.value', 'deg');

      cy.get(`[data-cy="periodMonths"]`).type('35');
      cy.get(`[data-cy="periodMonths"]`).should('have.value', '35');

      cy.get(`[data-cy="met"]`).should('not.be.checked');
      cy.get(`[data-cy="met"]`).click();
      cy.get(`[data-cy="met"]`).should('be.checked');

      cy.get(`[data-cy="credential"]`).select(1);

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        issuedClaim = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', issuedClaimPageUrlPattern);
    });
  });
});
