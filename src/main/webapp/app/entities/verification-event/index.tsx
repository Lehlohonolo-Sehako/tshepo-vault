import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import VerificationEvent from './verification-event';
import VerificationEventDeleteDialog from './verification-event-delete-dialog';
import VerificationEventDetail from './verification-event-detail';
import VerificationEventUpdate from './verification-event-update';

const VerificationEventRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<VerificationEvent />} />
    <Route path="new" element={<VerificationEventUpdate />} />
    <Route path=":id">
      <Route index element={<VerificationEventDetail />} />
      <Route path="edit" element={<VerificationEventUpdate />} />
      <Route path="delete" element={<VerificationEventDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default VerificationEventRoutes;
