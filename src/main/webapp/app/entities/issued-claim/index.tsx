import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import IssuedClaim from './issued-claim';
import IssuedClaimDeleteDialog from './issued-claim-delete-dialog';
import IssuedClaimDetail from './issued-claim-detail';
import IssuedClaimUpdate from './issued-claim-update';

const IssuedClaimRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<IssuedClaim />} />
    <Route path="new" element={<IssuedClaimUpdate />} />
    <Route path=":id">
      <Route index element={<IssuedClaimDetail />} />
      <Route path="edit" element={<IssuedClaimUpdate />} />
      <Route path="delete" element={<IssuedClaimDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default IssuedClaimRoutes;
