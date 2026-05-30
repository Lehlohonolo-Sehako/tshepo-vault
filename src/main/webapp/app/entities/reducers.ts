import bankConnection from 'app/entities/bank-connection/bank-connection.reducer';
import credential from 'app/entities/credential/credential.reducer';
import issuedClaim from 'app/entities/issued-claim/issued-claim.reducer';
import verificationEvent from 'app/entities/verification-event/verification-event.reducer';
import verifierApiKey from 'app/entities/verifier-api-key/verifier-api-key.reducer';
/* jhipster-needle-add-reducer-import - JHipster will add reducer here */

const entitiesReducers = {
  credential,
  issuedClaim,
  verifierApiKey,
  verificationEvent,
  bankConnection,
  /* jhipster-needle-add-reducer-combine - JHipster will add reducer here */
};

export default entitiesReducers;
