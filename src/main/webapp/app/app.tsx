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
  // Session still loading — show the branded splash so there's no blank flash.
  if (!sessionHasBeenFetched) {
    return (
      <div
        style={{
          position: 'fixed',
          inset: 0,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          background: '#f7f7f6',
          gap: 16,
          fontFamily: '"Plus Jakarta Sans", system-ui, sans-serif',
        }}
      >
        <svg width="48" height="48" viewBox="0 0 32 32" fill="none">
          <path
            d="M16 3.2l9 3.6v6.2c0 6-4 10.3-9 12-5-1.7-9-6-9-12V6.8l9-3.6z"
            fill="#00a9e0"
            fillOpacity="0.12"
            stroke="#00a9e0"
            strokeWidth="1.8"
          />
          <path d="M11.5 15.6l3 3 6-6.4" stroke="#00a9e0" strokeWidth="1.9" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
        <div style={{ fontSize: 20, color: '#1a1a1a', letterSpacing: -0.5 }}>
          tshepo<span style={{ color: '#00a9e0' }}>.</span>
        </div>
        <div
          style={{
            width: 28,
            height: 28,
            border: '2.5px solid rgba(0,169,224,0.18)',
            borderTopColor: '#00a9e0',
            borderRadius: '50%',
            animation: 'ts-spin 0.75s linear infinite',
          }}
        />
        <style>{`@keyframes ts-spin { to { transform: rotate(360deg); } }`}</style>
      </div>
    );
  }

  // Authenticated — show the holder app (hub, issue, present screens)
  if (isAuthenticated) {
    return (
      <>
        <ToastContainer position="top-left" className="toastify-container" toastClassName="toastify-toast" />
        <HolderApp />
      </>
    );
  }

  // /connect — show the ConnectScreen for unauthenticated users before OAuth
  if (location.pathname === '/connect') {
    return (
      <>
        <ToastContainer position="top-left" className="toastify-container" toastClassName="toastify-toast" />
        <HolderApp />
      </>
    );
  }

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
