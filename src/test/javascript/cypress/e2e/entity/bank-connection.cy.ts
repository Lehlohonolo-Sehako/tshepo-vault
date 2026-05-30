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

describe('BankConnection e2e test', () => {
  const bankConnectionPageUrl = '/bank-connection';
  const bankConnectionPageUrlPattern = new RegExp('/bank-connection(\\?.*)?$');
  let username: string;
  let password: string;
  const bankConnectionSample = { holderLogin: 'instead interestingly', connectedAt: '2026-05-29T10:55:32.682Z', status: 'CONNECTED' };

  let bankConnection;

  before(() => {
    cy.credentials().then(credentials => {
      ({ username, password } = credentials);
    });
  });

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/bank-connections+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/bank-connections').as('postEntityRequest');
    cy.intercept('DELETE', '/api/bank-connections/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (bankConnection) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/bank-connections/${bankConnection.id}`,
      }).then(() => {
        bankConnection = undefined;
      });
    }
  });

  it('BankConnections menu should load BankConnections page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('bank-connection');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('BankConnection').should('exist');
    cy.url().should('match', bankConnectionPageUrlPattern);
  });

  describe('BankConnection page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(bankConnectionPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create BankConnection page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/bank-connection/new$'));
        cy.getEntityCreateUpdateHeading('BankConnection');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', bankConnectionPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/bank-connections',
          body: bankConnectionSample,
        }).then(({ body }) => {
          bankConnection = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/bank-connections+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [bankConnection],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(bankConnectionPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details BankConnection page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('bankConnection');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', bankConnectionPageUrlPattern);
      });

      it('edit button click should load edit BankConnection page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('BankConnection');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', bankConnectionPageUrlPattern);
      });

      it('edit button click should load edit BankConnection page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('BankConnection');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', bankConnectionPageUrlPattern);
      });

      it('last delete button click should delete instance of BankConnection', () => {
        cy.intercept('GET', '/api/bank-connections/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('bankConnection').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', bankConnectionPageUrlPattern);

        bankConnection = undefined;
      });
    });
  });

  describe('new BankConnection page', () => {
    beforeEach(() => {
      cy.visit(bankConnectionPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('BankConnection');
    });

    it('should create an instance of BankConnection', () => {
      cy.get(`[data-cy="holderLogin"]`).type('grandson rapidly quarrelsomely');
      cy.get(`[data-cy="holderLogin"]`).should('have.value', 'grandson rapidly quarrelsomely');

      cy.get(`[data-cy="connectedAt"]`).type('2026-05-28T19:28');
      cy.get(`[data-cy="connectedAt"]`).blur();
      cy.get(`[data-cy="connectedAt"]`).should('have.value', '2026-05-28T19:28');

      cy.get(`[data-cy="status"]`).select('DISCONNECTED');

      cy.get(`[data-cy="maskedAccount"]`).type('huzzah');
      cy.get(`[data-cy="maskedAccount"]`).should('have.value', 'huzzah');

      cy.get(`[data-cy="accountType"]`).type('save');
      cy.get(`[data-cy="accountType"]`).should('have.value', 'save');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        bankConnection = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', bankConnectionPageUrlPattern);
    });
  });
});
