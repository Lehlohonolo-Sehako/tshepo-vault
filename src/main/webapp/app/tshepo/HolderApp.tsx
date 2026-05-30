import React, { useEffect, useState } from 'react';
import { QRCodeSVG } from 'qrcode.react';
import {
  Lock,
  Link as LinkIcon,
  CheckCircle,
  Check,
  Eye,
  Shield,
  Fingerprint,
  Building2,
  Scan,
  Copy,
  Plus,
  AlertCircle,
  Loader2,
} from 'lucide-react';
import { bankApi, credentialApi, ComputedClaim, CredentialResponse, ClaimThreshold } from './api';
import './tshepo.scss';

type Screen = 'connect' | 'hub' | 'issue' | 'present';

// ----- Design-token helpers -----

const Pill = ({ children, tone = 'brand' }: { children: React.ReactNode; tone?: string }) => (
  <span className={`ts-pill ts-pill--${tone}`}>{children}</span>
);

const Btn = ({
  children,
  variant = 'primary',
  size = 'md',
  full = false,
  disabled = false,
  onClick,
  icon: Icon,
  type = 'button',
}: {
  children?: React.ReactNode;
  variant?: string;
  size?: string;
  full?: boolean;
  disabled?: boolean;
  onClick?: () => void;
  icon?: React.ElementType;
  type?: 'button' | 'submit';
}) => (
  <button
    type={type}
    className={`ts-btn ts-btn--${variant} ts-btn--${size}${full ? ' ts-btn--full' : ''}`}
    disabled={disabled}
    onClick={onClick}
  >
    {Icon && <Icon size={size === 'sm' ? 14 : 16} />}
    {children}
  </button>
);

// ----- Connect Screen -----

const CONNECT_STEPS = [
  'Authorising read-only access',
  'Pulling 12 months of statements',
  'Computing claims in memory',
  'Discarding raw transactions',
];

function ConnectScreen({ onConnected }: { onConnected: () => void }) {
  const [phase, setPhase] = useState<'idle' | 'connecting' | 'done'>('idle');
  const [stepIdx, setStepIdx] = useState(0);
  const [error, setError] = useState('');

  useEffect(() => {
    if (phase !== 'connecting') return;
    if (stepIdx < CONNECT_STEPS.length) {
      const t = setTimeout(() => setStepIdx(i => i + 1), 680);
      return () => clearTimeout(t);
    }
    // Simulate OAuth exchange (fixture: any code works)
    bankApi
      .connect('fixture-code', window.location.origin + '/callback')
      .then(() => {
        setPhase('done');
        setTimeout(onConnected, 800);
      })
      .catch(() => {
        setError('Connection failed. Please try again.');
        setPhase('idle');
      });
  }, [phase, stepIdx]);

  return (
    <div className="ts-anim-in" style={{ maxWidth: 1040, margin: '0 auto', padding: '48px 24px' }}>
      <div
        style={{ display: 'grid', gridTemplateColumns: 'minmax(0,1.05fr) minmax(0,0.95fr)', gap: 28, alignItems: 'stretch' }}
        className="ts-connect-grid"
      >
        {/* Left — value prop */}
        <div>
          <Pill tone="brand">
            <Lock size={12} /> Privacy by design
          </Pill>
          <h1 style={{ fontSize: 34, lineHeight: 1.12, letterSpacing: '-0.5px', marginTop: 16, marginBottom: 14, fontWeight: 700 }}>
            Prove your finances
            <br />
            without revealing them.
          </h1>
          <p style={{ fontSize: 15.5, color: 'var(--ts-muted)', lineHeight: 1.6, maxWidth: 440 }}>
            Connect your Investec account once. Tshepo certifies the facts a landlord or lender needs — like &ldquo;average monthly inflow ≥
            R30,000&rdquo; — and nothing else.
          </p>
          <div style={{ marginTop: 32, display: 'flex', flexDirection: 'column', gap: 20, maxWidth: 440 }}>
            {[
              {
                icon: Eye,
                title: 'Read-only access',
                body: 'Tshepo can read your statements to compute claims. It can never move money or change anything.',
              },
              {
                icon: Shield,
                title: 'Raw data is never stored',
                body: 'Transactions are processed in memory to compute claims, then discarded. We keep the conclusions, not your spending.',
              },
              {
                icon: Fingerprint,
                title: "You decide what's shared",
                body: 'Each time you present a credential, you choose exactly which claims to reveal.',
              },
            ].map(({ icon: Icon, title, body }) => (
              <div key={title} style={{ display: 'flex', gap: 14 }}>
                <div
                  style={{
                    width: 36,
                    height: 36,
                    borderRadius: 10,
                    background: 'rgba(0,169,224,0.1)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    flexShrink: 0,
                  }}
                >
                  <Icon size={17} style={{ color: 'var(--ts-brand)' }} />
                </div>
                <div>
                  <div style={{ fontSize: 13.5, fontWeight: 600, marginBottom: 3 }}>{title}</div>
                  <div style={{ fontSize: 13, color: 'var(--ts-muted)', lineHeight: 1.55 }}>{body}</div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Right — connect card */}
        <div className="ts-card" style={{ padding: 28, display: 'flex', flexDirection: 'column' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <div
              style={{
                width: 48,
                height: 48,
                borderRadius: 14,
                background: 'var(--ts-charcoal)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <Building2 size={22} style={{ color: 'var(--ts-brand)' }} />
            </div>
            <div>
              <div style={{ fontSize: 15, fontWeight: 600 }}>Investec Programmable Banking</div>
              <div style={{ fontSize: 13, color: 'var(--ts-muted)' }}>Secure OAuth connection</div>
            </div>
          </div>

          <div
            style={{
              marginTop: 24,
              borderRadius: 12,
              border: '1px solid var(--ts-border)',
              background: 'rgba(247,247,246,0.6)',
              padding: 16,
              display: 'flex',
              flexDirection: 'column',
              gap: 12,
            }}
          >
            {CONNECT_STEPS.map((step, i) => {
              const active = phase === 'connecting' && i === stepIdx;
              const done = (phase === 'connecting' && i < stepIdx) || phase === 'done';
              return (
                <div key={i} className="ts-step">
                  <span
                    className={`ts-step__dot${done ? ' ts-step__dot--done' : active ? ' ts-step__dot--active' : ' ts-step__dot--idle'}`}
                  >
                    {done ? (
                      <Check size={13} strokeWidth={2.6} />
                    ) : active ? (
                      <span
                        style={{
                          width: 8,
                          height: 8,
                          borderRadius: '50%',
                          background: 'var(--ts-brand)',
                          display: 'block',
                          animation: 'pulse 1s infinite',
                        }}
                      />
                    ) : (
                      <span style={{ width: 8, height: 8, borderRadius: '50%', background: '#d7d7d4', display: 'block' }} />
                    )}
                  </span>
                  <span style={{ color: done || active ? 'var(--ts-charcoal)' : 'var(--ts-muted)' }}>{step}</span>
                </div>
              );
            })}
          </div>

          {error && (
            <div style={{ marginTop: 12, display: 'flex', alignItems: 'center', gap: 6, fontSize: 13, color: '#b91c1c' }}>
              <AlertCircle size={14} /> {error}
            </div>
          )}

          <div style={{ marginTop: 24, flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'flex-end' }}>
            {phase === 'done' ? (
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: 8,
                  height: 52,
                  color: 'var(--ts-success)',
                  fontWeight: 500,
                  fontSize: 15,
                }}
              >
                <CheckCircle size={20} /> Account connected
              </div>
            ) : (
              <Btn
                variant="dark"
                size="lg"
                full
                icon={phase === 'connecting' ? Loader2 : LinkIcon}
                disabled={phase === 'connecting'}
                onClick={() => {
                  setError('');
                  setPhase('connecting');
                  setStepIdx(0);
                }}
              >
                {phase === 'connecting' ? 'Connecting…' : 'Connect Investec account'}
              </Btn>
            )}
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: 6,
                fontSize: 12,
                color: 'var(--ts-muted)',
                marginTop: 14,
              }}
            >
              <Lock size={12} /> Bank-grade encryption · you can disconnect anytime
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

// ----- Credential Card -----

function ClaimPills({ claims, dark = false }: { claims?: ComputedClaim[]; dark?: boolean }) {
  if (!claims?.length) return null;
  return (
    <div style={{ display: 'flex', flexWrap: 'wrap', gap: 5, marginTop: 12 }}>
      {claims.map((c, i) => (
        <span
          key={i}
          style={{
            fontSize: 11,
            padding: '2px 8px',
            borderRadius: 100,
            fontWeight: 500,
            background: dark ? 'rgba(255,255,255,0.1)' : 'rgba(0,169,224,0.08)',
            color: dark ? 'rgba(255,255,255,0.7)' : 'var(--ts-brand)',
            border: dark ? '1px solid rgba(255,255,255,0.15)' : '1px solid rgba(0,169,224,0.2)',
          }}
        >
          {claimTypeShortLabel(c.type)}
        </span>
      ))}
    </div>
  );
}

function CredentialCard({ cred, style, onClick }: { cred: CredentialResponse; style: 'passport' | 'minimal'; onClick: () => void }) {
  const isExpired = cred.status === 'EXPIRED' || (cred.expiresAt && new Date(cred.expiresAt) < new Date());

  if (style === 'passport') {
    return (
      <div className="ts-cred-card ts-cred-card--passport" onClick={onClick}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', position: 'relative' }}>
          <div style={{ fontSize: 11, letterSpacing: '0.08em', opacity: 0.5, textTransform: 'uppercase', fontWeight: 600 }}>
            Proof of Funds
          </div>
          <span
            style={{
              fontSize: 11,
              padding: '2px 8px',
              borderRadius: 100,
              background: isExpired ? 'rgba(255,255,255,0.1)' : 'rgba(29,158,117,0.25)',
              color: isExpired ? 'rgba(255,255,255,0.5)' : '#6ee7bc',
            }}
          >
            {cred.status ?? 'ACTIVE'}
          </span>
        </div>
        <div style={{ fontSize: 18, fontWeight: 700, marginTop: 20, letterSpacing: '-0.3px', position: 'relative' }}>{cred.title}</div>
        {cred.purpose && <div style={{ fontSize: 12.5, opacity: 0.55, marginTop: 4, position: 'relative' }}>{cred.purpose}</div>}
        <ClaimPills claims={cred.claims} dark />
        <div
          style={{
            marginTop: 16,
            paddingTop: 14,
            borderTop: '1px solid rgba(255,255,255,0.1)',
            display: 'flex',
            justifyContent: 'flex-end',
            position: 'relative',
          }}
        >
          {cred.expiresAt && (
            <div style={{ fontSize: 11.5, opacity: 0.45 }}>Expires {new Date(cred.expiresAt).toLocaleDateString('en-ZA')}</div>
          )}
        </div>
      </div>
    );
  }

  return (
    <div className="ts-cred-card ts-cred-card--minimal" onClick={onClick}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div style={{ fontSize: 15, fontWeight: 600 }}>{cred.title}</div>
        <span className={`ts-pill ts-pill--${isExpired ? 'muted' : 'success'}`}>{cred.status ?? 'ACTIVE'}</span>
      </div>
      {cred.purpose && <div style={{ fontSize: 13, color: 'var(--ts-muted)', marginTop: 3 }}>{cred.purpose}</div>}
      <ClaimPills claims={cred.claims} />
      {cred.expiresAt && (
        <div style={{ marginTop: 10, fontSize: 12, color: 'var(--ts-muted)' }}>
          Expires {new Date(cred.expiresAt).toLocaleDateString('en-ZA')}
        </div>
      )}
    </div>
  );
}

// ----- Hub Screen -----

function HubScreen({ onIssue, onPresent }: { onIssue: () => void; onPresent: (cred: CredentialResponse) => void }) {
  const [credentials, setCredentials] = useState<CredentialResponse[]>([]);
  const [cardStyle, setCardStyle] = useState<'passport' | 'minimal'>('passport');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    credentialApi
      .list()
      .then(r => {
        setCredentials(r.data.credentials ?? []);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, []);

  return (
    <div className="ts-anim-in" style={{ maxWidth: 1100, margin: '0 auto', padding: '40px 24px' }}>
      <div
        style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'flex-end', justifyContent: 'space-between', gap: 16, marginBottom: 28 }}
      >
        <div>
          <h1 style={{ fontSize: 26, fontWeight: 700, letterSpacing: '-0.4px' }}>My credentials</h1>
          <div style={{ fontSize: 13.5, color: 'var(--ts-muted)', marginTop: 4 }}>
            {credentials.length} credential{credentials.length !== 1 ? 's' : ''} · bank-attested by Tshepo
          </div>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <div className="ts-toggle">
            <button className={cardStyle === 'passport' ? 'active' : ''} onClick={() => setCardStyle('passport')}>
              Passport
            </button>
            <button className={cardStyle === 'minimal' ? 'active' : ''} onClick={() => setCardStyle('minimal')}>
              Minimal
            </button>
          </div>
          <Btn variant="primary" size="md" icon={Plus} onClick={onIssue}>
            New credential
          </Btn>
        </div>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '60px 0', color: 'var(--ts-muted)' }}>
          <Loader2 size={24} style={{ animation: 'spin 1s linear infinite' }} />
        </div>
      ) : credentials.length === 0 ? (
        <div className="ts-card" style={{ padding: 56, textAlign: 'center' }}>
          <div style={{ fontSize: 15, fontWeight: 500, marginBottom: 8 }}>No credentials yet</div>
          <div style={{ fontSize: 13.5, color: 'var(--ts-muted)', marginBottom: 24 }}>
            Issue your first proof-of-funds credential to get started.
          </div>
          <Btn variant="primary" size="md" icon={Plus} onClick={onIssue}>
            Issue first credential
          </Btn>
        </div>
      ) : (
        <div className="ts-grid-3">
          {credentials.map(cred => (
            <CredentialCard key={cred.id} cred={cred} style={cardStyle} onClick={() => onPresent(cred)} />
          ))}
        </div>
      )}
    </div>
  );
}

// ----- Issue Screen -----

const CLAIM_CATALOGUE: {
  type: string;
  label: string;
  description: string;
  operator: string;
  threshold: number;
  currency?: string;
  periodMonths: number;
}[] = [
  {
    type: 'INFLOW',
    label: 'Average monthly inflow ≥ R30,000',
    description: 'Last 6 months',
    operator: 'GTE',
    threshold: 30000,
    currency: 'ZAR',
    periodMonths: 6,
  },
  {
    type: 'BALANCE',
    label: 'Current balance ≥ R50,000',
    description: 'As of today',
    operator: 'GTE',
    threshold: 50000,
    currency: 'ZAR',
    periodMonths: 1,
  },
  { type: 'TENURE', label: 'Account open ≥ 6 months', description: 'Account tenure', operator: 'GTE', threshold: 6, periodMonths: 12 },
  { type: 'TENURE', label: 'Account open ≥ 12 months', description: 'Account tenure', operator: 'GTE', threshold: 12, periodMonths: 12 },
  {
    type: 'SALARY_CONTINUITY',
    label: '3+ months consistent salary',
    description: 'Salary pattern',
    operator: 'GTE',
    threshold: 3,
    periodMonths: 6,
  },
  { type: 'NO_OVERDRAFT', label: 'No overdraft usage', description: 'Last 6 months', operator: 'EQ', threshold: 1, periodMonths: 6 },
];

function IssueScreen({ onBack, onIssued }: { onBack: () => void; onIssued: () => void }) {
  const [availableClaims, setAvailableClaims] = useState<ComputedClaim[]>([]);
  const [selected, setSelected] = useState<Set<number>>(new Set());
  const [title, setTitle] = useState('Proof of income');
  const [purpose, setPurpose] = useState('');
  const [loading, setLoading] = useState(true);
  const [issuing, setIssuing] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    bankApi
      .claims()
      .then(r => {
        setAvailableClaims(r.data.claims ?? []);
        setLoading(false);
      })
      .catch(() => {
        setAvailableClaims([]);
        setLoading(false);
      });
  }, []);

  const isClaimMet = (idx: number): boolean => {
    const cat = CLAIM_CATALOGUE[idx];
    const found = availableClaims.find(c => c.type === cat.type && Math.abs(c.threshold - cat.threshold) < 0.01);
    return found?.met ?? false;
  };

  const toggle = (idx: number) => {
    if (!isClaimMet(idx)) return;
    setSelected(prev => {
      const s = new Set(prev);
      if (s.has(idx)) {
        s.delete(idx);
      } else {
        s.add(idx);
      }
      return s;
    });
  };

  const issue = async () => {
    if (selected.size === 0 || !title.trim()) return;
    setIssuing(true);
    setError('');
    try {
      const claims: ClaimThreshold[] = [...selected].map(idx => {
        const c = CLAIM_CATALOGUE[idx];
        return { type: c.type, operator: c.operator, threshold: c.threshold, currency: c.currency, periodMonths: c.periodMonths };
      });
      await credentialApi.issue({ title: title.trim(), purpose: purpose.trim() || undefined, claims });
      onIssued();
    } catch (e: any) {
      setError(e?.response?.data?.detail ?? 'Failed to issue credential.');
    } finally {
      setIssuing(false);
    }
  };

  return (
    <div className="ts-anim-in" style={{ maxWidth: 780, margin: '0 auto', padding: '40px 24px' }}>
      <button
        onClick={onBack}
        style={{
          background: 'none',
          border: 'none',
          cursor: 'pointer',
          color: 'var(--ts-muted)',
          fontSize: 13.5,
          display: 'flex',
          alignItems: 'center',
          gap: 4,
          marginBottom: 24,
        }}
      >
        ← Back
      </button>
      <h1 style={{ fontSize: 24, fontWeight: 700, letterSpacing: '-0.3px', marginBottom: 6 }}>Issue new credential</h1>
      <p style={{ fontSize: 14, color: 'var(--ts-muted)', marginBottom: 28 }}>
        Select the claims to certify. Only met thresholds can be included.
      </p>

      <div className="ts-card ts-p-6 ts-mb-4">
        <div style={{ marginBottom: 16 }}>
          <label style={{ fontSize: 13, fontWeight: 500, display: 'block', marginBottom: 6 }}>
            Title <span style={{ color: '#b91c1c' }}>*</span>
          </label>
          <input
            className="ts-input"
            value={title}
            onChange={e => setTitle(e.target.value)}
            placeholder="e.g. Proof of income — rental application"
            maxLength={120}
          />
        </div>
        <div>
          <label style={{ fontSize: 13, fontWeight: 500, display: 'block', marginBottom: 6 }}>Purpose (optional)</label>
          <input
            className="ts-input"
            value={purpose}
            onChange={e => setPurpose(e.target.value)}
            placeholder="e.g. For a residential lease"
            maxLength={200}
          />
        </div>
      </div>

      <div className="ts-card ts-p-5 ts-mb-4">
        <div style={{ fontSize: 13, fontWeight: 600, marginBottom: 14 }}>Claims to include</div>
        {loading ? (
          <div style={{ textAlign: 'center', padding: '32px 0', color: 'var(--ts-muted)' }}>
            <Loader2 size={20} />
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
            {CLAIM_CATALOGUE.map((cat, idx) => {
              const met = isClaimMet(idx);
              const sel = selected.has(idx);
              return (
                <div
                  key={idx}
                  className={`ts-claim${sel ? ' ts-claim--selected' : ''}${!met ? ' ts-claim--disabled' : ''}`}
                  style={{ opacity: met ? 1 : 0.5, cursor: met ? 'pointer' : 'not-allowed' }}
                  onClick={() => toggle(idx)}
                >
                  <span className={met ? 'ts-claim--met-indicator' : 'ts-claim--unmet-indicator'} />
                  <div style={{ flex: 1 }}>
                    <div className="ts-claim__label">{cat.label}</div>
                    <div className="ts-claim__basis">{cat.description}</div>
                  </div>
                  <span className={`ts-pill ts-pill--${met ? 'success' : 'muted'} ts-claim__badge`}>{met ? 'Met' : 'Not met'}</span>
                  {sel && <Check size={15} style={{ color: 'var(--ts-brand)', flexShrink: 0 }} />}
                </div>
              );
            })}
          </div>
        )}
      </div>

      {error && (
        <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 13, color: '#b91c1c', marginBottom: 16 }}>
          <AlertCircle size={14} /> {error}
        </div>
      )}

      <Btn
        variant="primary"
        size="lg"
        full
        icon={issuing ? Loader2 : CheckCircle}
        disabled={selected.size === 0 || !title.trim() || issuing}
        onClick={issue}
      >
        {issuing ? 'Issuing…' : `Issue credential with ${selected.size} claim${selected.size !== 1 ? 's' : ''}`}
      </Btn>
    </div>
  );
}

// ----- Present Screen -----

function PresentScreen({ cred, onBack }: { cred: CredentialResponse; onBack: () => void }) {
  const [reveal, setReveal] = useState<Set<string>>(new Set());
  const [token, setToken] = useState('');
  const [copied, setCopied] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [detailedCred, setDetailedCred] = useState<CredentialResponse | null>(null);

  useEffect(() => {
    credentialApi
      .get(cred.id)
      .then(r => setDetailedCred(r.data))
      .catch(() => setDetailedCred(cred));
  }, [cred.id]);

  const claims = detailedCred?.claims ?? cred.claims ?? [];

  const toggle = (type: string) => {
    setReveal(prev => {
      const s = new Set(prev);
      if (s.has(type)) {
        s.delete(type);
      } else {
        s.add(type);
      }
      return s;
    });
    setToken('');
  };

  const generate = async () => {
    if (reveal.size === 0) return;
    setLoading(true);
    setError('');
    try {
      const r = await credentialApi.present(cred.id, { disclosedClaimTypes: [...reveal] });
      setToken(r.data.token);
    } catch (e: any) {
      setError(e?.response?.data?.detail ?? 'Failed to generate presentation.');
    } finally {
      setLoading(false);
    }
  };

  const copy = () => {
    navigator.clipboard?.writeText(token);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="ts-anim-in" style={{ maxWidth: 780, margin: '0 auto', padding: '40px 24px' }}>
      <button
        onClick={onBack}
        style={{
          background: 'none',
          border: 'none',
          cursor: 'pointer',
          color: 'var(--ts-muted)',
          fontSize: 13.5,
          display: 'flex',
          alignItems: 'center',
          gap: 4,
          marginBottom: 24,
        }}
      >
        ← Back
      </button>
      <h1 style={{ fontSize: 24, fontWeight: 700, letterSpacing: '-0.3px', marginBottom: 6 }}>{cred.title}</h1>
      <p style={{ fontSize: 14, color: 'var(--ts-muted)', marginBottom: 28 }}>
        Tick the claims to reveal. The verifier learns nothing about claims you keep hidden.
      </p>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 }}>
        {/* Claim toggles */}
        <div className="ts-card ts-p-5">
          <div style={{ fontSize: 13, fontWeight: 600, marginBottom: 14 }}>Select claims to reveal</div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
            {claims.length === 0 ? (
              <div style={{ fontSize: 13, color: 'var(--ts-muted)', textAlign: 'center', padding: '24px 0' }}>Loading claims…</div>
            ) : (
              claims.map(c => {
                const on = reveal.has(c.type);
                return (
                  <div key={c.type} className={`ts-claim${on ? ' ts-claim--selected' : ''}`} onClick={() => toggle(c.type)}>
                    <span
                      style={{
                        width: 18,
                        height: 18,
                        borderRadius: '50%',
                        border: '1.5px solid',
                        borderColor: on ? 'var(--ts-brand)' : 'var(--ts-border)',
                        background: on ? 'var(--ts-brand)' : 'transparent',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        flexShrink: 0,
                      }}
                    >
                      {on && <Check size={11} strokeWidth={3} style={{ color: '#fff' }} />}
                    </span>
                    <div style={{ flex: 1 }}>
                      <div className="ts-claim__label">{formatClaimLabel(c)}</div>
                    </div>
                  </div>
                );
              })
            )}
          </div>
          <div style={{ marginTop: 14, display: 'flex', alignItems: 'center', gap: 6, fontSize: 12, color: 'var(--ts-muted)' }}>
            <Lock size={13} style={{ color: 'var(--ts-success)' }} />
            Exact amounts are never disclosed — only that each threshold is met.
          </div>
        </div>

        {/* Preview / output */}
        <div className="ts-card" style={{ overflow: 'hidden' }}>
          {!token ? (
            <>
              <div
                style={{
                  padding: '14px 20px',
                  borderBottom: '1px solid var(--ts-border)',
                  display: 'flex',
                  alignItems: 'center',
                  gap: 8,
                  fontSize: 13,
                  color: 'var(--ts-muted)',
                }}
              >
                <Scan size={15} /> What the verifier will see
              </div>
              <div
                style={{
                  padding: 20,
                  background: 'rgba(247,247,246,0.4)',
                  minHeight: 200,
                  display: 'flex',
                  flexDirection: 'column',
                  gap: 10,
                }}
              >
                {claims.map(c => {
                  const on = reveal.has(c.type);
                  return (
                    <div
                      key={c.type}
                      style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: 10,
                        borderRadius: 10,
                        padding: '10px 12px',
                        fontSize: 12.5,
                        border: `1px ${on ? 'solid' : 'dashed'} var(--ts-border)`,
                        background: on ? 'var(--ts-white)' : 'rgba(255,255,255,0.4)',
                      }}
                    >
                      {on ? (
                        <CheckCircle size={15} style={{ color: 'var(--ts-success)', flexShrink: 0 }} />
                      ) : (
                        <Lock size={14} style={{ color: 'var(--ts-muted)', flexShrink: 0 }} />
                      )}
                      <span
                        style={{
                          color: on ? 'var(--ts-charcoal)' : 'var(--ts-muted)',
                          fontWeight: on ? 500 : 400,
                          filter: on ? 'none' : 'blur(3px)',
                        }}
                      >
                        {formatClaimLabel(c)}
                      </span>
                    </div>
                  );
                })}
              </div>
              <div style={{ padding: 20, borderTop: '1px solid var(--ts-border)' }}>
                <div style={{ fontSize: 12.5, color: 'var(--ts-muted)', marginBottom: 12 }}>
                  {reveal.size} of {claims.length} claims revealed
                </div>
                {error && <div style={{ fontSize: 13, color: '#b91c1c', marginBottom: 10 }}>{error}</div>}
                <Btn
                  variant="primary"
                  size="md"
                  full
                  icon={loading ? Loader2 : Scan}
                  disabled={reveal.size === 0 || loading}
                  onClick={generate}
                >
                  {loading ? 'Generating…' : 'Generate presentation'}
                </Btn>
              </div>
            </>
          ) : (
            <div style={{ padding: 24 }} className="ts-anim-in">
              <div style={{ fontSize: 13, color: 'var(--ts-muted)', marginBottom: 12, display: 'flex', alignItems: 'center', gap: 6 }}>
                <CheckCircle size={15} style={{ color: 'var(--ts-success)' }} /> Presentation ready
              </div>
              <div
                style={{
                  borderRadius: 12,
                  border: '1px solid var(--ts-border)',
                  padding: 16,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                <QRCodeSVG value={token} size={180} />
              </div>
              <div style={{ fontSize: 12.5, color: 'var(--ts-muted)', textAlign: 'center', marginTop: 12 }}>
                Scan, or share the token below
              </div>
              <div className="ts-token-box" style={{ marginTop: 12 }}>
                <code>{token.slice(0, 48)}…</code>
                <Btn variant="subtle" size="sm" icon={copied ? Check : Copy} onClick={copy}>
                  {copied ? 'Copied' : 'Copy'}
                </Btn>
              </div>
              <div
                style={{
                  marginTop: 16,
                  paddingTop: 16,
                  borderTop: '1px solid var(--ts-border)',
                  fontSize: 12.5,
                  color: 'var(--ts-muted)',
                  lineHeight: 1.6,
                }}
              >
                Reveals {reveal.size} claim{reveal.size !== 1 ? 's' : ''}. The verifier learns nothing beyond what you ticked.
              </div>
              <div style={{ marginTop: 14 }}>
                <Btn variant="ghost" size="md" full onClick={() => setToken('')}>
                  Adjust disclosure
                </Btn>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

function formatClaimLabel(c: ComputedClaim): string {
  const labels: Record<string, string> = {
    INFLOW: `Monthly inflow ≥ R${c.threshold?.toLocaleString()}`,
    BALANCE: `Balance ≥ R${c.threshold?.toLocaleString()}`,
    TENURE: `Account open ≥ ${c.threshold} months`,
    SALARY_CONTINUITY: `${c.threshold}+ months consistent salary`,
    NO_OVERDRAFT: 'No overdraft usage',
  };
  return labels[c.type] ?? c.type;
}

function claimTypeShortLabel(type: string): string {
  const labels: Record<string, string> = {
    INFLOW: 'Avg inflow',
    BALANCE: 'Balance',
    TENURE: 'Tenure',
    SALARY_CONTINUITY: 'Salary',
    NO_OVERDRAFT: 'No overdraft',
  };
  return labels[type] ?? type;
}

// ----- App Shell -----

export default function HolderApp() {
  const [screen, setScreen] = useState<Screen>('connect');
  const [selectedCred, setSelectedCred] = useState<CredentialResponse | null>(null);

  useEffect(() => {
    bankApi
      .status()
      .then(r => {
        if (r.data.connected) setScreen('hub');
      })
      .catch(() => {});
  }, []);

  return (
    <div className="ts-app">
      <nav className="ts-nav">
        <div className="ts-nav__brand">
          tshepo<span>.</span>
        </div>
        <div className="ts-nav__actions">
          <button
            onClick={() => {
              localStorage.removeItem('jhi-authenticationToken');
              sessionStorage.removeItem('jhi-authenticationToken');
              window.location.href = '/';
            }}
            className="ts-btn ts-btn--ghost ts-btn--sm"
            style={{ color: 'rgba(255,255,255,0.6)', borderColor: 'rgba(255,255,255,0.15)' }}
          >
            Sign out
          </button>
        </div>
      </nav>

      {screen === 'connect' && <ConnectScreen onConnected={() => setScreen('hub')} />}
      {screen === 'hub' && (
        <HubScreen
          onIssue={() => setScreen('issue')}
          onPresent={cred => {
            setSelectedCred(cred);
            setScreen('present');
          }}
        />
      )}
      {screen === 'issue' && <IssueScreen onBack={() => setScreen('hub')} onIssued={() => setScreen('hub')} />}
      {screen === 'present' && selectedCred && <PresentScreen cred={selectedCred} onBack={() => setScreen('hub')} />}
    </div>
  );
}
