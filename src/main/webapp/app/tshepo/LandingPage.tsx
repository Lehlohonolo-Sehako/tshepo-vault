import React, { useEffect, useRef, useState } from 'react';
import {
  ArrowRight,
  Building2,
  Check,
  CheckCircle,
  Clock,
  Eye,
  Fingerprint,
  Key,
  Link2,
  Lock,
  Scan,
  Shield,
  ShieldCheck,
  Sparkles,
} from 'lucide-react';
import './tshepo.scss';

// ── Scroll-reveal hook ──────────────────────────────────────────────────────

function useReveal(containerRef: React.RefObject<HTMLElement | null>) {
  useEffect(() => {
    const root = containerRef.current;
    if (!root) return;
    const els = Array.from(root.querySelectorAll<HTMLElement>('.ts-reveal'));
    const io = new IntersectionObserver(
      entries => {
        entries.forEach(e => {
          if (e.isIntersecting) {
            e.target.classList.add('in');
            io.unobserve(e.target);
          }
        });
      },
      { threshold: 0.12, rootMargin: '0px 0px -8% 0px' },
    );
    els.forEach(el => io.observe(el));
    const fallback = setTimeout(() => els.forEach(el => el.classList.add('in')), 1400);
    return () => {
      io.disconnect();
      clearTimeout(fallback);
    };
  }, []);
}

// ── Brand mark ──────────────────────────────────────────────────────────────

function TshepoMark({ size = 28, tone = 'brand' }: { size?: number; tone?: 'brand' | 'light' | 'dark' }) {
  const c = tone === 'light' ? '#FFFFFF' : tone === 'dark' ? '#1A1A1A' : '#00A9E0';
  return (
    <svg width={size} height={size} viewBox="0 0 32 32" fill="none">
      <path d="M16 3.2l9 3.6v6.2c0 6-4 10.3-9 12-5-1.7-9-6-9-12V6.8l9-3.6z" fill={c} fillOpacity="0.10" stroke={c} strokeWidth="1.8" />
      <path d="M11.5 15.6l3 3 6-6.4" stroke={c} strokeWidth="1.9" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

// ── Nav ─────────────────────────────────────────────────────────────────────

function Nav() {
  const [solid, setSolid] = useState(false);
  useEffect(() => {
    const onScroll = () => setSolid(window.scrollY > 12);
    window.addEventListener('scroll', onScroll, { passive: true });
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  return (
    <header className={`ts-lnav${solid ? ' ts-lnav--solid' : ''}`}>
      <div className="ts-lnav__inner">
        <div className="ts-wordmark">
          <TshepoMark size={26} tone="brand" />
          <span className="ts-wordmark__text">tshepo</span>
        </div>
        <nav className="ts-lnav__links">
          <a href="#how">How it works</a>
          <a href="#credential">The credential</a>
          <a href="#privacy">Privacy</a>
          <a href="#verify">For verifiers</a>
        </nav>
        <div className="ts-lnav__actions">
          <a href="/api/auth/investec/authorize" className="ts-btn ts-btn--ghost ts-btn--sm">
            Sign in
          </a>
          <a
            href="/api/auth/investec/authorize"
            className="ts-btn ts-btn--dark ts-btn--sm"
            style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}
          >
            Connect with Investec <ArrowRight size={14} />
          </a>
        </div>
      </div>
    </header>
  );
}

// ── Hero card (interactive) ─────────────────────────────────────────────────

function HeroCard() {
  const [revealInflow, setRevealInflow] = useState(true);
  const rows = [
    { label: 'Average monthly inflow ≥ R30,000', on: revealInflow, toggle: () => setRevealInflow(v => !v) },
    { label: 'Recurring salary ≥ 6 months', on: true, locked: true },
    { label: 'No overdraft in 12 months', on: false, locked: true },
  ];
  return (
    <div
      style={{
        borderRadius: 16,
        background: '#fff',
        border: '1px solid var(--ts-border)',
        boxShadow: '0 8px 28px rgba(0,0,0,0.08)',
        overflow: 'hidden',
      }}
    >
      {/* Header band */}
      <div className="ts-guilloche" style={{ background: '#1A1A1A', padding: '20px 24px 18px' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <TshepoMark size={22} tone="light" />
            <div style={{ lineHeight: 1 }}>
              <div style={{ color: '#fff', fontSize: 13 }}>tshepo</div>
              <div style={{ color: 'rgba(255,255,255,0.5)', fontSize: 10.5, marginTop: 4 }}>Verifiable credential</div>
            </div>
          </div>
          <span className="ts-pill ts-pill--success" style={{ fontSize: 11.5 }}>
            <CheckCircle size={12} /> Active
          </span>
        </div>
        <div style={{ marginTop: 16 }}>
          <div style={{ color: 'rgba(255,255,255,0.45)', fontSize: 10.5 }}>Holder</div>
          <div style={{ color: '#fff', fontSize: 18, marginTop: 4, letterSpacing: -0.3 }}>Thabo Mokoena</div>
        </div>
      </div>
      {/* Body */}
      <div style={{ padding: '20px 24px' }}>
        <div style={{ fontSize: 11, textTransform: 'uppercase', letterSpacing: '0.06em', color: 'var(--ts-muted)' }}>
          What the verifier sees
        </div>
        <div style={{ marginTop: 12, display: 'flex', flexDirection: 'column', gap: 8 }}>
          {rows.map((r, i) => (
            <div
              key={i}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: 12,
                borderRadius: 12,
                border: `1px ${r.on ? 'solid' : 'dashed'} var(--ts-border)`,
                padding: '10px 14px',
                background: r.on ? '#fff' : 'rgba(247,247,246,0.5)',
                transition: 'all 300ms',
              }}
            >
              <span
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  width: 28,
                  height: 28,
                  borderRadius: 8,
                  background: r.on ? 'rgba(29,158,117,0.12)' : '#fff',
                  border: r.on ? 'none' : '1px solid var(--ts-border)',
                  color: r.on ? 'var(--ts-success)' : 'var(--ts-muted)',
                  flexShrink: 0,
                }}
              >
                {r.on ? <Check size={15} strokeWidth={2.4} /> : <Lock size={14} />}
              </span>
              <span
                style={{
                  fontSize: 13,
                  flex: 1,
                  color: r.on ? 'var(--ts-charcoal)' : 'var(--ts-muted)',
                  fontWeight: r.on ? 500 : 400,
                  filter: r.on ? 'none' : 'blur(5px)',
                  userSelect: r.on ? 'auto' : 'none',
                }}
              >
                {r.label}
              </span>
              {!r.locked && (
                <button
                  onClick={r.toggle}
                  style={{
                    background: 'none',
                    border: 'none',
                    cursor: 'pointer',
                    fontSize: 11.5,
                    fontWeight: 500,
                    color: 'var(--ts-brand)',
                    flexShrink: 0,
                    padding: 0,
                    fontFamily: 'inherit',
                  }}
                >
                  {r.on ? 'Hide' : 'Reveal'}
                </button>
              )}
            </div>
          ))}
        </div>
        <div
          style={{
            marginTop: 16,
            paddingTop: 16,
            borderTop: '1px solid var(--ts-border)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            fontSize: 11.5,
          }}
        >
          <span style={{ display: 'flex', alignItems: 'center', gap: 6, color: 'var(--ts-muted)' }}>
            <Building2 size={13} /> Attested by Investec
          </span>
          <span style={{ display: 'flex', alignItems: 'center', gap: 6, color: 'var(--ts-muted)' }}>
            <Lock size={12} /> Amounts never shown
          </span>
        </div>
      </div>
    </div>
  );
}

// ── Hero ────────────────────────────────────────────────────────────────────

function Hero() {
  return (
    <section className="ts-hero-wash" style={{ position: 'relative', paddingTop: 68, overflow: 'hidden' }}>
      <div
        className="ts-grid-dots"
        style={{
          position: 'absolute',
          inset: 0,
          opacity: 0.6,
          pointerEvents: 'none',
          maskImage: 'linear-gradient(to bottom, black, transparent 80%)',
          WebkitMaskImage: 'linear-gradient(to bottom, black, transparent 80%)',
        }}
      />
      <div
        style={{
          position: 'relative',
          maxWidth: 1180,
          margin: '0 auto',
          padding: '80px 24px 96px',
          display: 'grid',
          gridTemplateColumns: 'minmax(0, 1.05fr) minmax(0, 0.95fr)',
          gap: 56,
          alignItems: 'center',
        }}
        className="ts-hero-grid"
      >
        {/* Copy */}
        <div className="ts-reveal in">
          <span className="ts-pill ts-pill--brand" style={{ fontSize: 12.5 }}>
            <Lock size={12} /> Built on Investec Programmable Banking
          </span>
          <h1
            style={{
              fontSize: 'clamp(38px, 5vw, 60px)',
              lineHeight: 1.05,
              letterSpacing: -1,
              marginTop: 20,
              marginBottom: 0,
              color: 'var(--ts-charcoal)',
              fontWeight: 700,
            }}
          >
            Prove your finances
            <br />
            without revealing them.
          </h1>
          <p style={{ fontSize: 17, color: 'var(--ts-muted)', lineHeight: 1.65, marginTop: 20, maxWidth: 500 }}>
            Tshepo turns your bank account into a private, bank-attested credential. Show a landlord or lender exactly what they need —
            &quot;average monthly inflow ≥ R30,000&quot; — and not a single transaction more.
          </p>
          <div style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', gap: 12, marginTop: 32 }}>
            <a
              href="/api/auth/investec/authorize"
              className="ts-btn ts-btn--primary ts-btn--lg"
              style={{ display: 'inline-flex', alignItems: 'center', gap: 8 }}
            >
              Connect with Investec <ArrowRight size={17} />
            </a>
            <a href="#how" className="ts-btn ts-btn--ghost ts-btn--lg">
              See how it works
            </a>
          </div>
          <div
            style={{
              display: 'flex',
              flexWrap: 'wrap',
              alignItems: 'center',
              gap: '8px 24px',
              marginTop: 28,
              fontSize: 13,
              color: 'var(--ts-muted)',
            }}
          >
            <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
              <Eye size={15} style={{ color: 'var(--ts-brand)' }} /> Read-only access
            </span>
            <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
              <ShieldCheck size={15} style={{ color: 'var(--ts-success)' }} /> Raw data never stored
            </span>
            <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
              <Fingerprint size={15} style={{ color: 'var(--ts-brand)' }} /> You choose what&apos;s shared
            </span>
          </div>
        </div>

        {/* Floating card */}
        <div className="ts-reveal in" style={{ position: 'relative' }}>
          <div
            style={{
              position: 'absolute',
              inset: -24,
              background: 'rgba(0,169,224,0.05)',
              borderRadius: 32,
              filter: 'blur(32px)',
              pointerEvents: 'none',
            }}
          />
          <div className="ts-float-card" style={{ position: 'relative', maxWidth: 420, margin: '0 auto' }}>
            <HeroCard />
          </div>
        </div>
      </div>

      <style>{`
        @media (max-width: 900px) {
          .ts-hero-grid { grid-template-columns: 1fr !important; }
        }
      `}</style>
    </section>
  );
}

// ── Trust strip ─────────────────────────────────────────────────────────────

const TRUST_ITEMS = [
  'Rental applications',
  'Vehicle finance',
  'Visa & proof of funds',
  'Tenant screening',
  'Lending affordability',
  'KYC onboarding',
];

function TrustStrip() {
  const loop = [...TRUST_ITEMS, ...TRUST_ITEMS];
  return (
    <section
      style={{
        borderTop: '1px solid var(--ts-border)',
        borderBottom: '1px solid var(--ts-border)',
        background: 'rgba(255,255,255,0.6)',
        padding: '24px 0',
        overflow: 'hidden',
      }}
    >
      <div style={{ maxWidth: 1180, margin: '0 auto', padding: '0 24px' }}>
        <div style={{ textAlign: 'center', fontSize: 12.5, color: 'var(--ts-muted)', marginBottom: 16 }}>
          One credential, trusted across every proof-of-funds moment
        </div>
        <div
          style={{
            position: 'relative',
            overflow: 'hidden',
            maskImage: 'linear-gradient(to right, transparent, black 8%, black 92%, transparent)',
            WebkitMaskImage: 'linear-gradient(to right, transparent, black 8%, black 92%, transparent)',
          }}
        >
          <div className="ts-marquee-track" style={{ display: 'flex', width: 'max-content', gap: 12 }}>
            {loop.map((t, i) => (
              <span
                key={i}
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: 8,
                  height: 36,
                  padding: '0 16px',
                  borderRadius: 100,
                  border: '1px solid var(--ts-border)',
                  background: '#fff',
                  fontSize: 13,
                  color: 'rgba(26,26,26,0.7)',
                  whiteSpace: 'nowrap',
                }}
              >
                <ShieldCheck size={14} style={{ color: 'var(--ts-brand)' }} /> {t}
              </span>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}

// ── How it works ─────────────────────────────────────────────────────────────

const HOW_STEPS = [
  {
    icon: Link2,
    n: '01',
    title: 'Connect your bank',
    body: 'Link your Investec account with read-only access. Tshepo can read statements to compute claims — never move money.',
  },
  {
    icon: ShieldCheck,
    n: '02',
    title: 'Issue a credential',
    body: 'Pick the facts to certify. Each is checked against your account live and signed by the bank as a verifiable credential.',
  },
  {
    icon: Scan,
    n: '03',
    title: 'Present selectively',
    body: 'Reveal only the claims a verifier needs as a QR or token. Everything else stays cryptographically hidden.',
  },
];

function HowItWorks() {
  return (
    <section id="how" className="ts-section">
      <div className="ts-reveal" style={{ maxWidth: 620 }}>
        <span className="ts-pill ts-pill--muted" style={{ fontSize: 12.5, display: 'inline-flex', gap: 6 }}>
          <Sparkles size={13} /> How it works
        </span>
        <h2
          style={{
            fontSize: 'clamp(28px, 3.4vw, 40px)',
            lineHeight: 1.1,
            letterSpacing: -0.5,
            marginTop: 16,
            color: 'var(--ts-charcoal)',
            fontWeight: 700,
          }}
        >
          From bank account to private proof in three steps.
        </h2>
      </div>
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(3, 1fr)',
          gap: 20,
          marginTop: 48,
        }}
        className="ts-how-grid"
      >
        {HOW_STEPS.map((s, i) => (
          <div key={s.n} className="ts-reveal ts-card" style={{ padding: 28, transitionDelay: `${i * 90}ms` }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <span
                style={{
                  width: 48,
                  height: 48,
                  borderRadius: 12,
                  background: 'rgba(0,169,224,0.1)',
                  color: 'var(--ts-brand)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                <s.icon size={22} />
              </span>
              <span style={{ fontSize: 28, letterSpacing: -0.5, color: 'var(--ts-border)', fontWeight: 700 }}>{s.n}</span>
            </div>
            <div style={{ fontSize: 18, fontWeight: 500, color: 'var(--ts-charcoal)', marginTop: 20 }}>{s.title}</div>
            <p style={{ fontSize: 14, color: 'var(--ts-muted)', lineHeight: 1.65, marginTop: 8 }}>{s.body}</p>
          </div>
        ))}
      </div>
      <style>{`@media(max-width:768px){.ts-how-grid{grid-template-columns:1fr!important}}`}</style>
    </section>
  );
}

// ── Credential showcase ──────────────────────────────────────────────────────

function CredentialShowcase() {
  return (
    <section
      id="credential"
      className="ts-guilloche"
      style={{ background: '#1A1A1A', color: '#fff', padding: '96px 0', overflow: 'hidden' }}
    >
      <div
        style={{
          maxWidth: 1180,
          margin: '0 auto',
          padding: '0 24px',
          display: 'grid',
          gridTemplateColumns: 'minmax(0, 0.92fr) minmax(0, 1.08fr)',
          gap: 56,
          alignItems: 'center',
        }}
        className="ts-showcase-grid"
      >
        <div className="ts-reveal">
          <span
            className="ts-pill"
            style={{
              background: 'rgba(0,169,224,0.15)',
              border: '1px solid rgba(0,169,224,0.25)',
              color: 'var(--ts-brand)',
              fontSize: 12.5,
              display: 'inline-flex',
              gap: 6,
            }}
          >
            <Shield size={13} /> The credential
          </span>
          <h2
            style={{
              fontSize: 'clamp(28px, 3.4vw, 40px)',
              lineHeight: 1.1,
              letterSpacing: -0.5,
              marginTop: 16,
              fontWeight: 700,
            }}
          >
            A premium digital ID for your finances.
          </h2>
          <p style={{ fontSize: 16, color: 'rgba(255,255,255,0.6)', lineHeight: 1.65, marginTop: 16, maxWidth: 480 }}>
            Every credential is signed by Investec and carries only conclusions — never your raw transactions. Verifiers trust the bank, not
            a PDF that can be forged.
          </p>
          <div style={{ marginTop: 32, display: 'flex', flexDirection: 'column', gap: 20, maxWidth: 460 }}>
            {[
              { icon: Building2, title: 'Bank-attested', body: 'Cryptographically signed by Investec as an SD-JWT verifiable credential.' },
              { icon: Key, title: 'Selectively disclosable', body: 'Reveal one claim or several — the rest stay provably hidden.' },
              { icon: Clock, title: 'Time-bound & revocable', body: 'Each credential carries an expiry and can be revoked at any time.' },
            ].map(({ icon: Icon, title, body }) => (
              <div key={title} className="ts-showcase-point">
                <div className="ts-showcase-point__icon">
                  <Icon size={18} />
                </div>
                <div>
                  <div className="ts-showcase-point__title">{title}</div>
                  <div className="ts-showcase-point__body">{body}</div>
                </div>
              </div>
            ))}
          </div>
        </div>
        {/* Static passport card mock */}
        <div className="ts-reveal" style={{ display: 'flex', justifyContent: 'center' }}>
          <div style={{ maxWidth: 460, width: '100%', pointerEvents: 'none' }}>
            <PassportCardMock />
          </div>
        </div>
      </div>
      <style>{`@media(max-width:900px){.ts-showcase-grid{grid-template-columns:1fr!important}}`}</style>
    </section>
  );
}

function PassportCardMock() {
  return (
    <div
      style={{
        borderRadius: 16,
        background: '#fff',
        border: '1px solid var(--ts-border)',
        boxShadow: '0 8px 28px rgba(0,0,0,0.18)',
        overflow: 'hidden',
      }}
    >
      <div
        className="ts-guilloche"
        style={{ background: '#1A1A1A', padding: '20px 24px 18px', borderBottom: '1px solid rgba(255,255,255,0.06)' }}
      >
        <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <TshepoMark size={24} tone="light" />
            <div style={{ lineHeight: 1 }}>
              <div style={{ color: '#fff', fontSize: 14 }}>tshepo</div>
              <div style={{ color: 'rgba(255,255,255,0.5)', fontSize: 10.5, marginTop: 4 }}>Verifiable credential</div>
            </div>
          </div>
          <span className="ts-pill ts-pill--success" style={{ fontSize: 11.5 }}>
            <CheckCircle size={12} /> Active
          </span>
        </div>
        <div style={{ marginTop: 20, display: 'flex', alignItems: 'flex-end', justifyContent: 'space-between' }}>
          <div>
            <div style={{ color: 'rgba(255,255,255,0.45)', fontSize: 11 }}>Holder</div>
            <div style={{ color: '#fff', fontSize: 20, marginTop: 4, letterSpacing: -0.3 }}>Thabo Mokoena</div>
          </div>
          <div style={{ textAlign: 'right' }}>
            <div style={{ color: 'rgba(255,255,255,0.45)', fontSize: 11 }}>Attested by</div>
            <div
              style={{
                color: '#fff',
                fontSize: 13.5,
                marginTop: 6,
                display: 'flex',
                alignItems: 'center',
                gap: 6,
                justifyContent: 'flex-end',
              }}
            >
              <Building2 size={14} style={{ color: 'var(--ts-brand)' }} /> Investec
            </div>
          </div>
        </div>
      </div>
      <div style={{ padding: '18px 24px' }}>
        <div style={{ fontSize: 15, fontWeight: 500, color: 'var(--ts-charcoal)' }}>Proof of income</div>
        <div style={{ fontSize: 13, color: 'var(--ts-muted)', marginTop: 2 }}>For residential lease application</div>
        <div style={{ marginTop: 14, display: 'flex', flexWrap: 'wrap', gap: 8 }}>
          {['Avg inflow ≥ R30k', 'Tenure ≥ 12 months', 'No overdraft'].map(label => (
            <span
              key={label}
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: 6,
                height: 28,
                padding: '0 10px',
                borderRadius: 8,
                fontSize: 12.5,
                fontWeight: 500,
                background: 'rgba(0,169,224,0.07)',
                border: '1px solid rgba(0,169,224,0.15)',
                color: 'var(--ts-charcoal)',
              }}
            >
              <ShieldCheck size={13} style={{ color: 'var(--ts-brand)' }} /> {label}
            </span>
          ))}
        </div>
        <div
          style={{
            marginTop: 16,
            paddingTop: 16,
            borderTop: '1px solid var(--ts-border)',
            display: 'grid',
            gridTemplateColumns: 'repeat(3, 1fr)',
            gap: 8,
            fontSize: 12.5,
          }}
        >
          {[
            { label: 'Claims', value: '3 attested' },
            { label: 'Issued', value: '1 May 2026' },
            { label: 'Valid until', value: '28 Oct 2026' },
          ].map(({ label, value }) => (
            <div key={label}>
              <div style={{ color: 'var(--ts-muted)' }}>{label}</div>
              <div style={{ color: 'var(--ts-charcoal)', fontWeight: 500, marginTop: 2 }}>{value}</div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

// ── Privacy ──────────────────────────────────────────────────────────────────

const PRIVACY_CARDS = [
  {
    icon: Eye,
    title: 'Read-only, always',
    body: "Tshepo connects through Investec's programmable banking with read access only. It can never initiate a payment or alter your account.",
  },
  {
    icon: ShieldCheck,
    title: 'Conclusions, not transactions',
    body: 'Your statements are processed in memory to compute each claim, then discarded. We keep the verdict — "threshold met" — not your spending.',
  },
  {
    icon: Lock,
    title: 'Thresholds, never amounts',
    body: 'A verifier sees "inflow ≥ R30,000", never "R42,300". The exact figure stays with you.',
  },
  {
    icon: Fingerprint,
    title: 'Disclosure on your terms',
    body: "Every presentation is built by you, claim by claim. Nothing is shared that you didn't explicitly tick.",
  },
];

function Privacy() {
  return (
    <section id="privacy" className="ts-section">
      <div className="ts-reveal" style={{ maxWidth: 640 }}>
        <span className="ts-pill ts-pill--brand" style={{ fontSize: 12.5, display: 'inline-flex', gap: 6 }}>
          <Lock size={13} /> Privacy by design
        </span>
        <h2
          style={{
            fontSize: 'clamp(28px, 3.4vw, 40px)',
            lineHeight: 1.1,
            letterSpacing: -0.5,
            marginTop: 16,
            color: 'var(--ts-charcoal)',
            fontWeight: 700,
          }}
        >
          Privacy isn&apos;t a setting. It&apos;s the architecture.
        </h2>
        <p style={{ fontSize: 16, color: 'var(--ts-muted)', lineHeight: 1.65, marginTop: 16 }}>
          Tshepo is built so that proving something true never means handing over everything true.
        </p>
      </div>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20, marginTop: 48 }} className="ts-privacy-grid">
        {PRIVACY_CARDS.map(({ icon: Icon, title, body }, i) => (
          <div key={title} className="ts-reveal ts-card" style={{ padding: 28, display: 'flex', gap: 16, transitionDelay: `${i * 70}ms` }}>
            <div
              style={{
                flexShrink: 0,
                width: 44,
                height: 44,
                borderRadius: 12,
                background: 'rgba(0,169,224,0.1)',
                color: 'var(--ts-brand)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <Icon size={20} />
            </div>
            <div>
              <div style={{ fontSize: 16, fontWeight: 500, color: 'var(--ts-charcoal)' }}>{title}</div>
              <p style={{ fontSize: 14, color: 'var(--ts-muted)', lineHeight: 1.65, marginTop: 6 }}>{body}</p>
            </div>
          </div>
        ))}
      </div>
      <style>{`@media(max-width:768px){.ts-privacy-grid{grid-template-columns:1fr!important}}`}</style>
    </section>
  );
}

// ── Verifier section ─────────────────────────────────────────────────────────

function VerifierSection() {
  return (
    <section id="verify" style={{ maxWidth: 1180, margin: '0 auto', padding: '0 24px 96px' }}>
      <div
        className="ts-reveal ts-verifier-grid"
        style={{
          borderRadius: 24,
          border: '1px solid var(--ts-border)',
          background: '#fff',
          boxShadow: '0 1px 3px rgba(0,0,0,0.06)',
          overflow: 'hidden',
          display: 'grid',
          gridTemplateColumns: '1fr 1fr',
        }}
      >
        <div style={{ padding: '44px' }}>
          <span className="ts-pill ts-pill--muted" style={{ fontSize: 12.5, display: 'inline-flex', gap: 6 }}>
            <Scan size={13} /> For verifiers
          </span>
          <h2
            style={{
              fontSize: 'clamp(24px, 2.8vw, 32px)',
              lineHeight: 1.12,
              letterSpacing: -0.5,
              marginTop: 16,
              color: 'var(--ts-charcoal)',
              fontWeight: 700,
            }}
          >
            Verify an applicant in seconds — no account needed.
          </h2>
          <p style={{ fontSize: 15, color: 'var(--ts-muted)', lineHeight: 1.65, marginTop: 14, maxWidth: 440 }}>
            Landlords and lenders paste a presentation and get a clear valid/invalid verdict showing only the disclosed claim. Screen at
            scale with the metered verification API.
          </p>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 12, marginTop: 28 }}>
            <a
              href="/verify"
              className="ts-btn ts-btn--primary ts-btn--md"
              style={{ display: 'inline-flex', alignItems: 'center', gap: 8 }}
            >
              Open the verifier <ArrowRight size={16} />
            </a>
            <a
              href="/api/auth/investec/authorize"
              className="ts-btn ts-btn--ghost ts-btn--md"
              style={{ display: 'inline-flex', alignItems: 'center', gap: 8 }}
            >
              <Key size={16} /> Get API access
            </a>
          </div>
        </div>
        <div
          className="ts-guilloche"
          style={{ background: '#1A1A1A', padding: '44px', display: 'flex', flexDirection: 'column', justifyContent: 'center' }}
        >
          <div
            style={{
              borderRadius: 16,
              border: '1px solid rgba(29,158,117,0.25)',
              overflow: 'hidden',
              background: '#fff',
            }}
          >
            <div
              style={{
                background: 'rgba(29,158,117,0.1)',
                padding: '16px 20px',
                display: 'flex',
                alignItems: 'center',
                gap: 12,
              }}
            >
              <span
                style={{
                  width: 40,
                  height: 40,
                  borderRadius: '50%',
                  background: 'var(--ts-success)',
                  color: '#fff',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                <Check size={22} strokeWidth={2.6} />
              </span>
              <div>
                <div style={{ fontSize: 15, fontWeight: 500, color: 'var(--ts-success)' }}>Valid credential</div>
                <div style={{ fontSize: 12, color: 'rgba(26,26,26,0.65)' }}>Attested by Investec</div>
              </div>
            </div>
            <div style={{ padding: 20 }}>
              <div style={{ fontSize: 11, textTransform: 'uppercase', letterSpacing: '0.06em', color: 'var(--ts-muted)' }}>
                Disclosed claim
              </div>
              <div
                style={{
                  marginTop: 10,
                  display: 'flex',
                  alignItems: 'center',
                  gap: 10,
                  borderRadius: 10,
                  border: '1px solid var(--ts-border)',
                  padding: '10px 14px',
                }}
              >
                <CheckCircle size={18} style={{ color: 'var(--ts-success)', flexShrink: 0 }} />
                <span style={{ fontSize: 14, fontWeight: 500, color: 'var(--ts-charcoal)' }}>Average monthly inflow ≥ R30,000</span>
              </div>
              <div
                style={{
                  marginTop: 12,
                  display: 'flex',
                  alignItems: 'flex-start',
                  gap: 8,
                  fontSize: 12,
                  color: 'var(--ts-muted)',
                  lineHeight: 1.6,
                }}
              >
                <Lock size={13} style={{ flexShrink: 0, marginTop: 1 }} /> Exact amounts and all other claims are not disclosed.
              </div>
            </div>
          </div>
          <div
            style={{
              marginTop: 16,
              borderRadius: 12,
              background: 'rgba(0,0,0,0.3)',
              border: '1px solid rgba(255,255,255,0.1)',
              padding: 14,
              fontFamily: 'monospace',
              fontSize: 11.5,
              lineHeight: 1.65,
              color: 'rgba(255,255,255,0.8)',
            }}
          >
            <div>
              <span style={{ color: 'var(--ts-brand)' }}>POST</span> /v1/verify
            </div>
            <div style={{ color: 'var(--ts-success)', marginTop: 4 }}>→ 200 · {'{ valid: true, claims: [...] }'}</div>
          </div>
        </div>
      </div>
      <style>{`@media(max-width:768px){.ts-verifier-grid{grid-template-columns:1fr!important}}`}</style>
    </section>
  );
}

// ── CTA ─────────────────────────────────────────────────────────────────────

function CTA() {
  return (
    <section style={{ maxWidth: 1180, margin: '0 auto', padding: '0 24px 96px' }}>
      <div
        className="ts-reveal ts-guilloche"
        style={{
          position: 'relative',
          borderRadius: 24,
          background: '#1A1A1A',
          overflow: 'hidden',
          textAlign: 'center',
          padding: '80px 24px',
        }}
      >
        <div
          className="ts-grid-dots"
          style={{
            position: 'absolute',
            inset: 0,
            opacity: 0.12,
            pointerEvents: 'none',
          }}
        />
        <div style={{ position: 'relative' }}>
          <TshepoMark size={44} tone="light" />
          <h2
            style={{
              fontSize: 'clamp(28px, 3.6vw, 42px)',
              lineHeight: 1.08,
              letterSpacing: -0.8,
              marginTop: 20,
              color: '#fff',
              maxWidth: 680,
              marginLeft: 'auto',
              marginRight: 'auto',
              fontWeight: 700,
            }}
          >
            Your bank already knows you&apos;re good for it.
            <br />
            Now you can prove it — privately.
          </h2>
          <div style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', justifyContent: 'center', gap: 12, marginTop: 32 }}>
            <a
              href="/api/auth/investec/authorize"
              className="ts-btn ts-btn--primary ts-btn--lg"
              style={{ display: 'inline-flex', alignItems: 'center', gap: 8 }}
            >
              Connect with Investec <ArrowRight size={17} />
            </a>
            <a
              href="/api/auth/investec/authorize"
              className="ts-btn ts-btn--ghost ts-btn--lg"
              style={{ color: '#fff', borderColor: 'rgba(255,255,255,0.2)', background: 'transparent' }}
            >
              Explore the demo
            </a>
          </div>
          <div style={{ fontSize: 12.5, color: 'rgba(255,255,255,0.45)', marginTop: 24 }}>
            Tshepo means &quot;trust&quot; in Sesotho and Setswana.
          </div>
        </div>
      </div>
    </section>
  );
}

// ── Footer ───────────────────────────────────────────────────────────────────

const FOOTER_COLS = [
  { h: 'Product', links: ['How it works', 'The credential', 'Privacy', 'Pricing'] },
  { h: 'Verifiers', links: ['Open verifier', 'API docs', 'Pricing', 'Status'] },
  { h: 'Company', links: ['About', 'Security', 'Contact', 'Careers'] },
];

function Footer() {
  return (
    <footer style={{ borderTop: '1px solid var(--ts-border)', background: '#fff' }}>
      <div
        style={{
          maxWidth: 1180,
          margin: '0 auto',
          padding: '56px 24px',
          display: 'grid',
          gridTemplateColumns: '1.4fr 1fr 1fr 1fr',
          gap: 40,
        }}
        className="ts-footer-grid"
      >
        <div>
          <div className="ts-wordmark" style={{ display: 'inline-flex' }}>
            <TshepoMark size={26} tone="brand" />
            <span className="ts-wordmark__text" style={{ marginLeft: 10 }}>
              tshepo
            </span>
          </div>
          <p style={{ fontSize: 13.5, color: 'var(--ts-muted)', lineHeight: 1.65, marginTop: 16, maxWidth: 260 }}>
            Bank-attested proof of funds, built on Investec Programmable Banking. Prove what&apos;s true without revealing everything true.
          </p>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginTop: 20, fontSize: 12, color: 'var(--ts-muted)' }}>
            <Lock size={14} style={{ color: 'var(--ts-success)' }} /> Bank-grade encryption
          </div>
        </div>
        {FOOTER_COLS.map(col => (
          <div key={col.h}>
            <div style={{ fontSize: 13, fontWeight: 500, color: 'var(--ts-charcoal)' }}>{col.h}</div>
            <ul style={{ marginTop: 14, listStyle: 'none', padding: 0, display: 'flex', flexDirection: 'column', gap: 10 }}>
              {col.links.map(l => (
                <li key={l}>
                  <a href="/api/auth/investec/authorize" style={{ fontSize: 13.5, color: 'var(--ts-muted)', textDecoration: 'none' }}>
                    {l}
                  </a>
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>
      <div style={{ borderTop: '1px solid var(--ts-border)' }}>
        <div
          style={{
            maxWidth: 1180,
            margin: '0 auto',
            padding: '20px 24px',
            display: 'flex',
            flexWrap: 'wrap',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: 12,
            fontSize: 12.5,
            color: 'var(--ts-muted)',
          }}
        >
          <span>© 2026 Tshepo. A proof-of-funds credential issuer.</span>
          <div style={{ display: 'flex', alignItems: 'center', gap: 20 }}>
            <a href="#" style={{ color: 'inherit', textDecoration: 'none' }}>
              Privacy
            </a>
            <a href="#" style={{ color: 'inherit', textDecoration: 'none' }}>
              Terms
            </a>
            <a href="#" style={{ color: 'inherit', textDecoration: 'none' }}>
              Security
            </a>
          </div>
        </div>
      </div>
      <style>{`@media(max-width:768px){.ts-footer-grid{grid-template-columns:1fr 1fr!important}}`}</style>
    </footer>
  );
}

// ── Page ────────────────────────────────────────────────────────────────────

export default function LandingPage() {
  const containerRef = useRef<HTMLDivElement>(null);
  useReveal(containerRef);

  return (
    <div className="ts-landing" ref={containerRef}>
      <Nav />
      <Hero />
      <TrustStrip />
      <HowItWorks />
      <CredentialShowcase />
      <Privacy />
      <VerifierSection />
      <CTA />
      <Footer />
    </div>
  );
}
