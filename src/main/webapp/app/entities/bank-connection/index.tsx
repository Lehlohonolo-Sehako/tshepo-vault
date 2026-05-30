import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import BankConnection from './bank-connection';
import BankConnectionDeleteDialog from './bank-connection-delete-dialog';
import BankConnectionDetail from './bank-connection-detail';
import BankConnectionUpdate from './bank-connection-update';

const BankConnectionRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<BankConnection />} />
    <Route path="new" element={<BankConnectionUpdate />} />
    <Route path=":id">
      <Route index element={<BankConnectionDetail />} />
      <Route path="edit" element={<BankConnectionUpdate />} />
      <Route path="delete" element={<BankConnectionDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default BankConnectionRoutes;
