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

describe('VerificationEvent e2e test', () => {
  const verificationEventPageUrl = '/verification-event';
  const verificationEventPageUrlPattern = new RegExp('/verification-event(\\?.*)?$');
  let username: string;
  let password: string;
  const verificationEventSample = { verifiedAt: '2026-05-29T16:09:35.560Z', result: 'MALFORMED' };

  let verificationEvent;

  before(() => {
    cy.credentials().then(credentials => {
      ({ username, password } = credentials);
    });
  });

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/verification-events+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/verification-events').as('postEntityRequest');
    cy.intercept('DELETE', '/api/verification-events/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (verificationEvent) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/verification-events/${verificationEvent.id}`,
      }).then(() => {
        verificationEvent = undefined;
      });
    }
  });

  it('VerificationEvents menu should load VerificationEvents page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('verification-event');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('VerificationEvent').should('exist');
    cy.url().should('match', verificationEventPageUrlPattern);
  });

  describe('VerificationEvent page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(verificationEventPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create VerificationEvent page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/verification-event/new$'));
        cy.getEntityCreateUpdateHeading('VerificationEvent');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', verificationEventPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/verification-events',
          body: verificationEventSample,
        }).then(({ body }) => {
          verificationEvent = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/verification-events+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/verification-events?page=0&size=20>; rel="last",<http://localhost/api/verification-events?page=0&size=20>; rel="first"',
              },
              body: [verificationEvent],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(verificationEventPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details VerificationEvent page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('verificationEvent');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', verificationEventPageUrlPattern);
      });

      it('edit button click should load edit VerificationEvent page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('VerificationEvent');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', verificationEventPageUrlPattern);
      });

      it('edit button click should load edit VerificationEvent page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('VerificationEvent');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', verificationEventPageUrlPattern);
      });

      it('last delete button click should delete instance of VerificationEvent', () => {
        cy.intercept('GET', '/api/verification-events/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('verificationEvent').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', verificationEventPageUrlPattern);

        verificationEvent = undefined;
      });
    });
  });

  describe('new VerificationEvent page', () => {
    beforeEach(() => {
      cy.visit(verificationEventPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('VerificationEvent');
    });

    it('should create an instance of VerificationEvent', () => {
      cy.get(`[data-cy="verifiedAt"]`).type('2026-05-29T02:12');
      cy.get(`[data-cy="verifiedAt"]`).blur();
      cy.get(`[data-cy="verifiedAt"]`).should('have.value', '2026-05-29T02:12');

      cy.get(`[data-cy="result"]`).select('UNTRUSTED_ISSUER');

      cy.get(`[data-cy="disclosedClaims"]`).type('../fake-data/blob/hipster.txt');
      cy.get(`[data-cy="disclosedClaims"]`).invoke('val').should('match', new RegExp('../fake-data/blob/hipster.txt'));

      cy.get(`[data-cy="credentialRef"]`).type('oh accountability avaricious');
      cy.get(`[data-cy="credentialRef"]`).should('have.value', 'oh accountability avaricious');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        verificationEvent = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', verificationEventPageUrlPattern);
    });
  });
});
