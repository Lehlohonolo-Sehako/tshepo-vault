import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import VerifierApiKey from './verifier-api-key';
import VerifierApiKeyDeleteDialog from './verifier-api-key-delete-dialog';
import VerifierApiKeyDetail from './verifier-api-key-detail';
import VerifierApiKeyUpdate from './verifier-api-key-update';

const VerifierApiKeyRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<VerifierApiKey />} />
    <Route path="new" element={<VerifierApiKeyUpdate />} />
    <Route path=":id">
      <Route index element={<VerifierApiKeyDetail />} />
      <Route path="edit" element={<VerifierApiKeyUpdate />} />
      <Route path="delete" element={<VerifierApiKeyDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default VerifierApiKeyRoutes;
