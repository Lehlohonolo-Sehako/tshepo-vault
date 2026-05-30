import 'react-toastify/dist/ReactToastify.css';
import './app.scss';
import './tshepo/tshepo.scss';
import 'app/config/dayjs';

import React, { useEffect } from 'react';
import { Card } from 'react-bootstrap';
import { BrowserRouter, useLocation } from 'react-router';

import { ToastContainer } from 'react-toastify';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import AppRoutes from 'app/routes';
import { hasAnyAuthority } from 'app/shared/auth/private-route';
import ErrorBoundary from 'app/shared/error/error-boundary';
import { Authority } from 'app/shared/jhipster/constants';
import Footer from 'app/shared/layout/footer/footer';
import Header from 'app/shared/layout/header/header';
import { getProfile } from 'app/shared/reducers/application-profile';
import { getSession } from 'app/shared/reducers/authentication';
import HolderApp from 'app/tshepo/HolderApp';
import VerifierPage from 'app/tshepo/VerifierPage';

const baseHref = document.querySelector('base').getAttribute('href').replace(/\/$/, '');

const AppInner = () => {
  const location = useLocation();
  const isAuthenticated = useAppSelector(state => state.authentication.isAuthenticated);
  const sessionHasBeenFetched = useAppSelector(state => state.authentication.sessionHasBeenFetched);
  const isAdmin = useAppSelector(state => hasAnyAuthority(state.authentication.account.authorities, [Authority.ADMIN]));
  const ribbonEnv = useAppSelector(state => state.applicationProfile.ribbonEnv);
  const isInProduction = useAppSelector(state => state.applicationProfile.inProduction);
  const isOpenAPIEnabled = useAppSelector(state => state.applicationProfile.isOpenAPIEnabled);

  if (location.pathname === '/verify') {
    return <VerifierPage />;
  }

  if (sessionHasBeenFetched && isAuthenticated) {
    return (
      <>
        <ToastContainer position="top-left" className="toastify-container" toastClassName="toastify-toast" />
        <HolderApp />
      </>
    );
  }

  return (
    <div className="app-container" style={{ paddingTop: '60px' }}>
      <ToastContainer position="top-left" className="toastify-container" toastClassName="toastify-toast" />
      <ErrorBoundary>
        <Header
          isAuthenticated={isAuthenticated}
          isAdmin={isAdmin}
          ribbonEnv={ribbonEnv}
          isInProduction={isInProduction}
          isOpenAPIEnabled={isOpenAPIEnabled}
        />
      </ErrorBoundary>
      <div className="container-fluid view-container" id="app-view-container">
        <Card className="jh-card">
          <ErrorBoundary>
            <AppRoutes />
          </ErrorBoundary>
        </Card>
        <Footer />
      </div>
    </div>
  );
};

export const App = () => {
  const dispatch = useAppDispatch();

  useEffect(() => {
    dispatch(getSession());
    dispatch(getProfile());
  }, []);

  return (
    <BrowserRouter basename={baseHref}>
      <AppInner />
    </BrowserRouter>
  );
};

export default App;
