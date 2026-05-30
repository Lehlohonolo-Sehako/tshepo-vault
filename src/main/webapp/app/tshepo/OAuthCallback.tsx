import React, { useEffect } from 'react';
import { useNavigate } from 'react-router';
import { Storage } from 'react-jhipster';

import { useAppDispatch } from 'app/config/store';
import { getSession } from 'app/shared/reducers/authentication';

const AUTH_TOKEN_KEY = 'jhi-authenticationToken';

export default function OAuthCallback() {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();

  useEffect(() => {
    const token = new URLSearchParams(window.location.search).get('token');
    if (token) {
      Storage.local.set(AUTH_TOKEN_KEY, token);
      dispatch(getSession());
    }
    navigate('/', { replace: true });
  }, []);

  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        height: '100vh',
        fontFamily: '"Plus Jakarta Sans", sans-serif',
        fontSize: 15,
        color: '#6b6b6b',
      }}
    >
      Signing you in…
    </div>
  );
}
