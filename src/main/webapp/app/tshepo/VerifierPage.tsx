import React, { useState } from 'react';
import { Shield, CheckCircle, XCircle, Loader2, ChevronRight } from 'lucide-react';
import { verifyApi, DisclosedClaim, VerifyResponse } from './api';

function ClaimRow({ claim }: { claim: DisclosedClaim }) {
  const label = formatClaimLabel(claim);
  return (
    <div className="ts-claim" style={{ cursor: 'default' }}>
      <span className={claim.met ? 'ts-claim--met-indicator' : 'ts-claim--unmet-indicator'} />
      <span className="ts-claim__label">{label}</span>
      <span className={`ts-pill ${claim.met ? 'ts-pill--success' : 'ts-pill--warning'} ts-claim__badge`}>
        {claim.met ? 'Met' : 'Not met'}
      </span>
    </div>
  );
}

function formatClaimLabel(c: DisclosedClaim): string {
  const op = c.operator === 'GTE' ? '≥' : c.operator === 'LTE' ? '≤' : c.operator;
  const cur = c.currency ?? 'ZAR';
  switch (c.type) {
    case 'BALANCE':
      return `Balance ${op} ${cur} ${c.threshold.toLocaleString()}`;
    case 'INFLOW':
      return `Monthly inflow ${op} ${cur} ${c.threshold.toLocaleString()}`;
    case 'TENURE':
      return `Account tenure ${op} ${c.threshold} months`;
    case 'SALARY_CONTINUITY':
      return `Regular salary for ${c.threshold}+ months`;
    case 'NO_OVERDRAFT':
      return 'No overdraft usage';
    default:
      return `${c.type} ${op} ${c.threshold}`;
  }
}

export default function VerifierPage() {
  const [token, setToken] = useState('');
  const [result, setResult] = useState<VerifyResponse | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  async function handleVerify() {
    if (!token.trim()) return;
    setBusy(true);
    setError('');
    setResult(null);
    try {
      const res = await verifyApi.verify(token.trim());
      setResult(res.data);
    } catch {
      setError('Verification failed. The token may be invalid or expired.');
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="ts-app" style={{ minHeight: '100vh' }}>
      {/* Nav */}
      <nav className="ts-nav">
        <div className="ts-nav__brand">
          tshepo<span>.</span>verify
        </div>
        <a href="/" className="ts-btn ts-btn--ghost ts-btn--sm" style={{ textDecoration: 'none' }}>
          Holder login
        </a>
      </nav>

      <div className="ts-container ts-page-pad">
        <div style={{ maxWidth: 560, margin: '0 auto' }}>
          <div className="ts-flex ts-items-center ts-gap-3 ts-mb-4">
            <div
              style={{
                width: 44,
                height: 44,
                borderRadius: 12,
                background: 'rgba(0,169,224,0.1)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <Shield size={22} color="var(--ts-brand)" />
            </div>
            <div>
              <h1 style={{ fontSize: 22, fontWeight: 700, margin: 0 }}>Verify proof of funds</h1>
              <p className="ts-text-sm ts-text-muted" style={{ margin: 0 }}>
                Paste a presentation token to verify bank-attested claims
              </p>
            </div>
          </div>

          <div className="ts-card ts-p-5">
            <label className="ts-text-sm ts-font-medium" style={{ display: 'block', marginBottom: 8 }}>
              Presentation token
            </label>
            <textarea
              className="ts-textarea"
              rows={5}
              placeholder="Paste the SD-JWT presentation token here…"
              value={token}
              onChange={e => setToken(e.target.value)}
              style={{ fontFamily: 'monospace', fontSize: 12 }}
            />
            {error && (
              <p className="ts-text-sm" style={{ color: '#b91c1c', marginTop: 8 }}>
                {error}
              </p>
            )}
            <button
              className="ts-btn ts-btn--primary ts-btn--md ts-btn--full ts-mt-4"
              onClick={handleVerify}
              disabled={busy || !token.trim()}
            >
              {busy ? <Loader2 size={15} className="spin" /> : <ChevronRight size={15} />}
              {busy ? 'Verifying…' : 'Verify token'}
            </button>
          </div>

          {result && (
            <div className="ts-card ts-p-5 ts-mt-4 ts-anim-in">
              <div className="ts-flex ts-items-center ts-gap-3 ts-mb-4">
                {result.valid ? <CheckCircle size={28} color="var(--ts-success)" /> : <XCircle size={28} color="#b91c1c" />}
                <div>
                  <p style={{ fontWeight: 700, fontSize: 16, margin: 0 }}>{result.valid ? 'Valid presentation' : 'Invalid presentation'}</p>
                  {result.error && (
                    <p className="ts-text-sm ts-text-muted" style={{ margin: 0 }}>
                      {result.error}
                    </p>
                  )}
                </div>
              </div>

              {result.valid && (
                <>
                  <hr className="ts-divider" />
                  <div className="ts-flex ts-justify-between ts-mb-3">
                    <span className="ts-text-sm ts-text-muted">Issuer DID</span>
                    <span className="ts-text-sm ts-font-medium" style={{ wordBreak: 'break-all', maxWidth: '65%', textAlign: 'right' }}>
                      {result.issuerDid ?? '—'}
                    </span>
                  </div>
                  {result.expiresAt && (
                    <div className="ts-flex ts-justify-between ts-mb-3">
                      <span className="ts-text-sm ts-text-muted">Expires</span>
                      <span className="ts-text-sm ts-font-medium">
                        {new Date(result.expiresAt).toLocaleDateString('en-ZA', { dateStyle: 'medium' })}
                      </span>
                    </div>
                  )}

                  {result.disclosedClaims && result.disclosedClaims.length > 0 && (
                    <>
                      <hr className="ts-divider" />
                      <p className="ts-text-sm ts-font-medium ts-mb-3">Disclosed claims</p>
                      <div className="ts-flex ts-flex-col ts-gap-2">
                        {result.disclosedClaims.map((c, i) => (
                          <ClaimRow key={i} claim={c} />
                        ))}
                      </div>
                    </>
                  )}
                </>
              )}
            </div>
          )}
        </div>
      </div>

      <style>{`.spin { animation: spin 1s linear infinite; } @keyframes spin { to { transform: rotate(360deg); } }`}</style>
    </div>
  );
}
