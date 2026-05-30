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

describe('VerifierApiKey e2e test', () => {
  const verifierApiKeyPageUrl = '/verifier-api-key';
  const verifierApiKeyPageUrlPattern = new RegExp('/verifier-api-key(\\?.*)?$');
  let username: string;
  let password: string;
  const verifierApiKeySample = {
    ownerLogin: 'tough disrespect yet',
    label: 'soliloquy ouch overvalue',
    keyHash: 'haze',
    active: false,
    createdAt: '2026-05-28T23:42:01.587Z',
  };

  let verifierApiKey;

  before(() => {
    cy.credentials().then(credentials => {
      ({ username, password } = credentials);
    });
  });

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/verifier-api-keys+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/verifier-api-keys').as('postEntityRequest');
    cy.intercept('DELETE', '/api/verifier-api-keys/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (verifierApiKey) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/verifier-api-keys/${verifierApiKey.id}`,
      }).then(() => {
        verifierApiKey = undefined;
      });
    }
  });

  it('VerifierApiKeys menu should load VerifierApiKeys page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('verifier-api-key');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('VerifierApiKey').should('exist');
    cy.url().should('match', verifierApiKeyPageUrlPattern);
  });

  describe('VerifierApiKey page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(verifierApiKeyPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create VerifierApiKey page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/verifier-api-key/new$'));
        cy.getEntityCreateUpdateHeading('VerifierApiKey');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', verifierApiKeyPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/verifier-api-keys',
          body: verifierApiKeySample,
        }).then(({ body }) => {
          verifierApiKey = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/verifier-api-keys+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [verifierApiKey],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(verifierApiKeyPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details VerifierApiKey page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('verifierApiKey');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', verifierApiKeyPageUrlPattern);
      });

      it('edit button click should load edit VerifierApiKey page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('VerifierApiKey');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', verifierApiKeyPageUrlPattern);
      });

      it('edit button click should load edit VerifierApiKey page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('VerifierApiKey');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', verifierApiKeyPageUrlPattern);
      });

      it('last delete button click should delete instance of VerifierApiKey', () => {
        cy.intercept('GET', '/api/verifier-api-keys/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('verifierApiKey').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', verifierApiKeyPageUrlPattern);

        verifierApiKey = undefined;
      });
    });
  });

  describe('new VerifierApiKey page', () => {
    beforeEach(() => {
      cy.visit(verifierApiKeyPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('VerifierApiKey');
    });

    it('should create an instance of VerifierApiKey', () => {
      cy.get(`[data-cy="ownerLogin"]`).type('worriedly beautifully brand');
      cy.get(`[data-cy="ownerLogin"]`).should('have.value', 'worriedly beautifully brand');

      cy.get(`[data-cy="label"]`).type('gosh');
      cy.get(`[data-cy="label"]`).should('have.value', 'gosh');

      cy.get(`[data-cy="keyHash"]`).type('absentmindedly');
      cy.get(`[data-cy="keyHash"]`).should('have.value', 'absentmindedly');

      cy.get(`[data-cy="active"]`).should('not.be.checked');
      cy.get(`[data-cy="active"]`).click();
      cy.get(`[data-cy="active"]`).should('be.checked');

      cy.get(`[data-cy="callCount"]`).type('7478');
      cy.get(`[data-cy="callCount"]`).should('have.value', '7478');

      cy.get(`[data-cy="createdAt"]`).type('2026-05-29T12:23');
      cy.get(`[data-cy="createdAt"]`).blur();
      cy.get(`[data-cy="createdAt"]`).should('have.value', '2026-05-29T12:23');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        verifierApiKey = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', verifierApiKeyPageUrlPattern);
    });
  });
});
