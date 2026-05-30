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

describe('Credential e2e test', () => {
  const credentialPageUrl = '/credential';
  const credentialPageUrlPattern = new RegExp('/credential(\\?.*)?$');
  let username: string;
  let password: string;
  const credentialSample = {
    holderLogin: 'yuck triangular',
    title: 'why',
    status: 'REVOKED',
    issuedAt: '2026-05-29T06:26:42.646Z',
    expiresAt: '2026-05-29T07:40:33.705Z',
    issuerDid: 'recommendation',
    sdJwt: 'Li4vZmFrZS1kYXRhL2Jsb2IvaGlwc3Rlci50eHQ=',
  };

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
    cy.intercept('GET', '/api/credentials+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/credentials').as('postEntityRequest');
    cy.intercept('DELETE', '/api/credentials/*').as('deleteEntityRequest');
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

  it('Credentials menu should load Credentials page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('credential');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Credential').should('exist');
    cy.url().should('match', credentialPageUrlPattern);
  });

  describe('Credential page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(credentialPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Credential page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/credential/new$'));
        cy.getEntityCreateUpdateHeading('Credential');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', credentialPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/credentials',
          body: credentialSample,
        }).then(({ body }) => {
          credential = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/credentials+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/credentials?page=0&size=20>; rel="last",<http://localhost/api/credentials?page=0&size=20>; rel="first"',
              },
              body: [credential],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(credentialPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Credential page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('credential');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', credentialPageUrlPattern);
      });

      it('edit button click should load edit Credential page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Credential');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', credentialPageUrlPattern);
      });

      it('edit button click should load edit Credential page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Credential');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', credentialPageUrlPattern);
      });

      it('last delete button click should delete instance of Credential', () => {
        cy.intercept('GET', '/api/credentials/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('credential').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', credentialPageUrlPattern);

        credential = undefined;
      });
    });
  });

  describe('new Credential page', () => {
    beforeEach(() => {
      cy.visit(credentialPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Credential');
    });

    it('should create an instance of Credential', () => {
      cy.get(`[data-cy="holderLogin"]`).type('exasperation efface');
      cy.get(`[data-cy="holderLogin"]`).should('have.value', 'exasperation efface');

      cy.get(`[data-cy="title"]`).type('weighty hollow consequently');
      cy.get(`[data-cy="title"]`).should('have.value', 'weighty hollow consequently');

      cy.get(`[data-cy="purpose"]`).type('grass pish');
      cy.get(`[data-cy="purpose"]`).should('have.value', 'grass pish');

      cy.get(`[data-cy="status"]`).select('REVOKED');

      cy.get(`[data-cy="issuedAt"]`).type('2026-05-28T20:23');
      cy.get(`[data-cy="issuedAt"]`).blur();
      cy.get(`[data-cy="issuedAt"]`).should('have.value', '2026-05-28T20:23');

      cy.get(`[data-cy="expiresAt"]`).type('2026-05-28T23:19');
      cy.get(`[data-cy="expiresAt"]`).blur();
      cy.get(`[data-cy="expiresAt"]`).should('have.value', '2026-05-28T23:19');

      cy.get(`[data-cy="issuerDid"]`).type('energetically whereas ugh');
      cy.get(`[data-cy="issuerDid"]`).should('have.value', 'energetically whereas ugh');

      cy.get(`[data-cy="sdJwt"]`).type('../fake-data/blob/hipster.txt');
      cy.get(`[data-cy="sdJwt"]`).invoke('val').should('match', new RegExp('../fake-data/blob/hipster.txt'));

      cy.get(`[data-cy="claimsSummary"]`).type('yahoo or');
      cy.get(`[data-cy="claimsSummary"]`).should('have.value', 'yahoo or');

      cy.get(`[data-cy="vcRef"]`).type('molasses');
      cy.get(`[data-cy="vcRef"]`).should('have.value', 'molasses');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        credential = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', credentialPageUrlPattern);
    });
  });
});
