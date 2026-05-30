import React from 'react';
import { Route } from 'react-router'; // eslint-disable-line

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import BankConnection from './bank-connection';
import Credential from './credential';
import IssuedClaim from './issued-claim';
import VerificationEvent from './verification-event';
import VerifierApiKey from './verifier-api-key';
/* jhipster-needle-add-route-import - JHipster will add routes here */

export default () => {
  return (
    <div>
      <ErrorBoundaryRoutes>
        {/* prettier-ignore */}
        <Route path="/credential/*" element={<Credential />} />
        <Route path="/issued-claim/*" element={<IssuedClaim />} />
        <Route path="/verifier-api-key/*" element={<VerifierApiKey />} />
        <Route path="/verification-event/*" element={<VerificationEvent />} />
        <Route path="/bank-connection/*" element={<BankConnection />} />
        {/* jhipster-needle-add-route-path - JHipster will add routes here */}
      </ErrorBoundaryRoutes>
    </div>
  );
};
