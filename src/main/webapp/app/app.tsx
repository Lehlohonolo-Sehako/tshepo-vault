import 'react-toastify/dist/ReactToastify.css';
import './app.scss';
import './tshepo/tshepo.scss';
import 'app/config/dayjs';

import React, { useEffect } from 'react';
import { BrowserRouter, useLocation } from 'react-router';

import { ToastContainer } from 'react-toastify';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getProfile } from 'app/shared/reducers/application-profile';
import { getSession } from 'app/shared/reducers/authentication';
import HolderApp from 'app/tshepo/HolderApp';
import LandingPage from 'app/tshepo/LandingPage';
import OAuthCallback from 'app/tshepo/OAuthCallback';
import VerifierPage from 'app/tshepo/VerifierPage';

const baseHref = document.querySelector('base').getAttribute('href').replace(/\/$/, '');

const AppInner = () => {
  const location = useLocation();
  const isAuthenticated = useAppSelector(state => state.authentication.isAuthenticated);
  const sessionHasBeenFetched = useAppSelector(state => state.authentication.sessionHasBeenFetched);

  // OAuth callback — must be handled before the auth check so the token can be stored.
  if (location.pathname === '/oauth-callback') {
    return <OAuthCallback />;
  }

  // Public verifier page — no auth required.
  if (location.pathname === '/verify') {
    return <VerifierPage />;
  }

  // Authenticated holder — render the full holder app.
  if (sessionHasBeenFetched && isAuthenticated) {
    return (
      <>
        <ToastContainer position="top-left" className="toastify-container" toastClassName="toastify-toast" />
        <HolderApp />
      </>
    );
  }

  // Session still loading — render nothing to avoid a flash of the landing page
  // for users who are already logged in.
  if (!sessionHasBeenFetched) {
    return null;
  }

  // Not authenticated (any path) — always show the landing page.
  // This replaces /login, /account/register, and all other JHipster auth routes.
  return <LandingPage />;
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
