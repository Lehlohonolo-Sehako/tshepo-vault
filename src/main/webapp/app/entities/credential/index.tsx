import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Credential from './credential';
import CredentialDeleteDialog from './credential-delete-dialog';
import CredentialDetail from './credential-detail';
import CredentialUpdate from './credential-update';

const CredentialRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Credential />} />
    <Route path="new" element={<CredentialUpdate />} />
    <Route path=":id">
      <Route index element={<CredentialDetail />} />
      <Route path="edit" element={<CredentialUpdate />} />
      <Route path="delete" element={<CredentialDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default CredentialRoutes;
