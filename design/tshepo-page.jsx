/* Tshepo — marketing landing page. Reuses icons, ui primitives, and the PassportCard. */
const { useState, useEffect, useRef } = React;

/* Scroll-reveal: adds .in when element enters viewport. */
function useReveal() {
  useEffect(() => {
    const els = Array.from(document.querySelectorAll('.reveal'));
    const io = new IntersectionObserver((entries) => {
      entries.forEach((e) => { if (e.isIntersecting) { e.target.classList.add('in'); io.unobserve(e.target); } });
    }, { threshold: 0.12, rootMargin: '0px 0px -8% 0px' });
    els.forEach((el) => io.observe(el));
    // Safety net: never leave content offset/hidden if observer or transition stalls
    // (e.g. backgrounded preview contexts where transitions don't progress).
    const fallback = setTimeout(() => els.forEach((el) => el.classList.add('in')), 1400);
    return () => { io.disconnect(); clearTimeout(fallback); };
  }, []);
}

const APP_URL = 'index.html';

/* ---------------- NAV ---------------- */
function Nav() {
  const [solid, setSolid] = useState(false);
  useEffect(() => {
    const onScroll = () => setSolid(window.scrollY > 12);
    window.addEventListener('scroll', onScroll);
    return () => window.removeEventListener('scroll', onScroll);
  }, []);
  return (
    <header className={`fixed top-0 inset-x-0 z-50 transition-all duration-300 ${solid ? 'bg-white/85 backdrop-blur-md border-b border-line' : 'bg-transparent border-b border-transparent'}`}>
      <div className="max-w-[1180px] mx-auto px-6 h-[68px] flex items-center justify-between">
        <TshepoWordmark tone="dark" />
        <nav className="hidden md:flex items-center gap-8 text-[14px] text-charcoal/75">
          <a href="#how" className="hover:text-charcoal transition-colors">How it works</a>
          <a href="#credential" className="hover:text-charcoal transition-colors">The credential</a>
          <a href="#privacy" className="hover:text-charcoal transition-colors">Privacy</a>
          <a href="#verify" className="hover:text-charcoal transition-colors whitespace-nowrap">For verifiers</a>
        </nav>
        <div className="flex items-center gap-2.5">
          <a href={APP_URL}><Button variant="quiet" size="sm" className="hidden sm:inline-flex">Sign in</Button></a>
          <a href={APP_URL}><Button variant="dark" size="sm" iconRight={IconArrowRight}>Get started</Button></a>
        </div>
      </div>
    </header>
  );
}

/* ---------------- HERO ---------------- */
function Hero() {
  const [revealInflow, setRevealInflow] = useState(true);
  return (
    <section className="relative hero-wash pt-[68px] overflow-hidden">
      <div className="absolute inset-0 grid-dots opacity-60 pointer-events-none" style={{ maskImage: 'linear-gradient(to bottom, black, transparent 80%)' }} />
      <div className="relative max-w-[1180px] mx-auto px-6 pt-20 pb-24 grid lg:grid-cols-[1.05fr_0.95fr] gap-14 items-center">
        {/* copy */}
        <div className="reveal in">
          <Pill tone="brand" icon={IconLock}>Built on Investec Programmable Banking</Pill>
          <h1 className="text-[clamp(38px,5vw,60px)] leading-[1.05] tracking-tight mt-5 text-charcoal">
            Prove your finances<br />without revealing them.
          </h1>
          <p className="text-[17px] text-muted leading-relaxed mt-5 max-w-[500px]">
            Tshepo turns your bank account into a private, bank-attested credential.
            Show a landlord or lender exactly what they need — “average monthly inflow ≥ R30,000” —
            and not a single transaction more.
          </p>
          <div className="flex flex-wrap items-center gap-3 mt-8">
            <a href={APP_URL}><Button variant="primary" size="lg" iconRight={IconArrowRight}>Connect your account</Button></a>
            <a href="#how"><Button variant="ghost" size="lg">See how it works</Button></a>
          </div>
          <div className="flex flex-wrap items-center gap-x-6 gap-y-2 mt-7 text-[13px] text-muted">
            <span className="flex items-center gap-2"><IconEye size={15} className="text-brand" /> Read-only access</span>
            <span className="flex items-center gap-2"><IconShieldCheck size={15} className="text-success" /> Raw data never stored</span>
            <span className="flex items-center gap-2"><IconFingerprint size={15} className="text-brand" /> You choose what’s shared</span>
          </div>
        </div>

        {/* visual — floating passport card + selective disclosure demo */}
        <div className="reveal in relative">
          <div className="absolute -inset-6 bg-brand/[0.05] rounded-[32px] blur-2xl pointer-events-none" />
          <div className="relative float-card max-w-[420px] mx-auto">
            <HeroCard revealInflow={revealInflow} setRevealInflow={setRevealInflow} />
          </div>
        </div>
      </div>
    </section>
  );
}

/* A compact, self-contained hero credential card with a live reveal toggle. */
function HeroCard({ revealInflow, setRevealInflow }) {
  const rows = [
    { label: 'Average monthly inflow ≥ R30,000', on: revealInflow, toggle: () => setRevealInflow((v) => !v) },
    { label: 'Recurring salary ≥ 6 months', on: true, locked: true },
    { label: 'No overdraft in 12 months', on: false, locked: true },
  ];
  return (
    <div className="rounded-2xl bg-white border border-line shadow-lift overflow-hidden">
      <div className="bg-charcoal guilloche px-6 pt-5 pb-[18px]">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <TshepoMark size={22} tone="light" />
            <div className="leading-none">
              <div className="text-white text-[13px]">tshepo</div>
              <div className="text-white/50 text-[10px] mt-1">Verifiable credential</div>
            </div>
          </div>
          <Pill tone="success" icon={IconCheckCircle}>Active</Pill>
        </div>
        <div className="mt-4">
          <div className="text-white/45 text-[10.5px]">Holder</div>
          <div className="text-white text-[18px] mt-1 tracking-tight">Thabo Mokoena</div>
        </div>
      </div>
      <div className="px-6 py-5">
        <div className="text-[11px] uppercase tracking-wide text-muted">What the verifier sees</div>
        <div className="mt-3 space-y-2">
          {rows.map((r, i) => (
            <div key={i} className={`flex items-center gap-3 rounded-xl border px-3.5 py-3 transition-all duration-300 ${r.on ? 'bg-white border-line' : 'bg-page/50 border-dashed border-line'}`}>
              <span className={`flex items-center justify-center w-7 h-7 rounded-lg shrink-0 ${r.on ? 'bg-success/12 text-success' : 'bg-white border border-line text-muted'}`}>
                {r.on ? <IconCheck size={15} stroke={2.4} /> : <IconLock size={14} />}
              </span>
              <span className={`text-[13px] flex-1 ${r.on ? 'text-charcoal font-medium' : 'text-muted masked-text'}`}>{r.label}</span>
              {!r.locked && (
                <button onClick={r.toggle} className="text-[11.5px] font-medium text-brand hover:text-brandhover transition-colors shrink-0">
                  {r.on ? 'Hide' : 'Reveal'}
                </button>
              )}
            </div>
          ))}
        </div>
        <div className="mt-4 pt-4 border-t border-line flex items-center justify-between text-[11.5px]">
          <span className="flex items-center gap-1.5 text-muted"><IconBank size={13} /> Attested by Investec</span>
          <span className="flex items-center gap-1.5 text-muted"><IconLock size={12} /> Amounts never shown</span>
        </div>
      </div>
    </div>
  );
}

/* ---------------- TRUST STRIP ---------------- */
function TrustStrip() {
  const items = ['Rental applications', 'Vehicle finance', 'Visa & proof of funds', 'Tenant screening', 'Lending affordability', 'KYC onboarding'];
  const loop = [...items, ...items];
  return (
    <section className="border-y border-line bg-white/60 py-6 overflow-hidden">
      <div className="max-w-[1180px] mx-auto px-6">
        <div className="text-center text-[12.5px] text-muted mb-4">One credential, trusted across every proof-of-funds moment</div>
        <div className="relative overflow-hidden" style={{ maskImage: 'linear-gradient(to right, transparent, black 8%, black 92%, transparent)' }}>
          <div className="flex marquee w-max gap-3">
            {loop.map((t, i) => (
              <span key={i} className="flex items-center gap-2 h-9 px-4 rounded-full border border-line bg-white text-[13px] text-charcoal/70 whitespace-nowrap">
                <IconShieldCheck size={14} className="text-brand" /> {t}
              </span>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}

/* ---------------- HOW IT WORKS ---------------- */
function HowItWorks() {
  const steps = [
    { n: '01', icon: IconLink, title: 'Connect your bank', body: 'Link your Investec account with read-only access. Tshepo can read statements to compute claims — never move money.' },
    { n: '02', icon: IconShieldCheck, title: 'Issue a credential', body: 'Pick the facts to certify. Each is checked against your account live and signed by the bank as a verifiable credential.' },
    { n: '03', icon: IconScan, title: 'Present selectively', body: 'Reveal only the claims a verifier needs as a QR or token. Everything else stays cryptographically hidden.' },
  ];
  return (
    <section id="how" className="max-w-[1180px] mx-auto px-6 py-24">
      <div className="reveal max-w-[620px]">
        <Pill tone="neutral" icon={IconSparkle}>How it works</Pill>
        <h2 className="text-[clamp(28px,3.4vw,40px)] leading-[1.1] tracking-tight mt-4 text-charcoal">From bank account to private proof in three steps.</h2>
      </div>
      <div className="grid md:grid-cols-3 gap-5 mt-12">
        {steps.map((s, i) => (
          <div key={s.n} className="reveal rounded-2xl bg-white border border-line shadow-card p-7" style={{ transitionDelay: `${i * 90}ms` }}>
            <div className="flex items-center justify-between">
              <span className="w-12 h-12 rounded-xl bg-brand/10 text-brand flex items-center justify-center"><s.icon size={22} /></span>
              <span className="text-[28px] tracking-tight text-line">{s.n}</span>
            </div>
            <div className="text-[18px] font-medium text-charcoal mt-5">{s.title}</div>
            <p className="text-[14px] text-muted leading-relaxed mt-2">{s.body}</p>
          </div>
        ))}
      </div>
    </section>
  );
}

/* ---------------- CREDENTIAL SHOWCASE ---------------- */
function CredentialShowcase() {
  const sample = INITIAL_CREDENTIALS[0];
  return (
    <section id="credential" className="bg-charcoal guilloche text-white py-24 overflow-hidden">
      <div className="max-w-[1180px] mx-auto px-6 grid lg:grid-cols-[0.92fr_1.08fr] gap-14 items-center">
        <div className="reveal">
          <Pill tone="brand" icon={IconShield} className="bg-brand/15 border-brand/25 text-brand">The credential</Pill>
          <h2 className="text-[clamp(28px,3.4vw,40px)] leading-[1.1] tracking-tight mt-4">A premium digital ID for your finances.</h2>
          <p className="text-[16px] text-white/60 leading-relaxed mt-4 max-w-[480px]">
            Every credential is signed by Investec and carries only conclusions — never your raw transactions.
            Verifiers trust the bank, not a PDF that can be forged.
          </p>
          <div className="mt-8 space-y-5 max-w-[460px]">
            <ShowcasePoint icon={IconBank} title="Bank-attested">Cryptographically signed by Investec as an SD-JWT verifiable credential.</ShowcasePoint>
            <ShowcasePoint icon={IconKey} title="Selectively disclosable">Reveal one claim or several — the rest stay provably hidden.</ShowcasePoint>
            <ShowcasePoint icon={IconClock} title="Time-bound & revocable">Each credential carries an expiry and can be revoked at any time.</ShowcasePoint>
          </div>
        </div>
        <div className="reveal">
          <div className="max-w-[460px] mx-auto lg:ml-auto [&_*]:!cursor-default">
            <div className="pointer-events-none">
              <PassportCard cred={sample} onPresent={() => {}} onDownload={() => {}} />
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
function ShowcasePoint({ icon: Icon, title, children }) {
  return (
    <div className="flex gap-3.5">
      <div className="shrink-0 w-9 h-9 rounded-[11px] bg-white/10 text-brand flex items-center justify-center mt-0.5"><Icon size={18} /></div>
      <div>
        <div className="text-[15px] font-medium text-white">{title}</div>
        <div className="text-[14px] text-white/55 leading-relaxed mt-0.5">{children}</div>
      </div>
    </div>
  );
}

/* ---------------- PRIVACY ---------------- */
function Privacy() {
  const cards = [
    { icon: IconEye, title: 'Read-only, always', body: 'Tshepo connects through Investec’s programmable banking with read access only. It can never initiate a payment or alter your account.' },
    { icon: IconShieldCheck, title: 'Conclusions, not transactions', body: 'Your statements are processed in memory to compute each claim, then discarded. We keep the verdict — “threshold met” — not your spending.' },
    { icon: IconLock, title: 'Thresholds, never amounts', body: 'A verifier sees “inflow ≥ R30,000”, never “R42,300”. The exact figure stays with you.' },
    { icon: IconFingerprint, title: 'Disclosure on your terms', body: 'Every presentation is built by you, claim by claim. Nothing is shared that you didn’t explicitly tick.' },
  ];
  return (
    <section id="privacy" className="max-w-[1180px] mx-auto px-6 py-24">
      <div className="reveal max-w-[640px]">
        <Pill tone="brand" icon={IconLock}>Privacy by design</Pill>
        <h2 className="text-[clamp(28px,3.4vw,40px)] leading-[1.1] tracking-tight mt-4 text-charcoal">Privacy isn’t a setting. It’s the architecture.</h2>
        <p className="text-[16px] text-muted leading-relaxed mt-4">Tshepo is built so that proving something true never means handing over everything true.</p>
      </div>
      <div className="grid sm:grid-cols-2 gap-5 mt-12">
        {cards.map((c, i) => (
          <div key={i} className="reveal rounded-2xl bg-white border border-line shadow-card p-7 flex gap-4" style={{ transitionDelay: `${i * 70}ms` }}>
            <div className="shrink-0 w-11 h-11 rounded-xl bg-brand/10 text-brand flex items-center justify-center"><c.icon size={20} /></div>
            <div>
              <div className="text-[16px] font-medium text-charcoal">{c.title}</div>
              <p className="text-[14px] text-muted leading-relaxed mt-1.5">{c.body}</p>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}

/* ---------------- VERIFIER / API ---------------- */
function VerifierSection() {
  return (
    <section id="verify" className="max-w-[1180px] mx-auto px-6 pb-24">
      <div className="reveal rounded-3xl border border-line bg-white shadow-card overflow-hidden grid lg:grid-cols-2">
        <div className="p-9 lg:p-11">
          <Pill tone="neutral" icon={IconScan}>For verifiers</Pill>
          <h2 className="text-[clamp(24px,2.8vw,32px)] leading-[1.12] tracking-tight mt-4 text-charcoal">Verify an applicant in seconds — no account needed.</h2>
          <p className="text-[15px] text-muted leading-relaxed mt-3.5 max-w-[440px]">
            Landlords and lenders paste a presentation and get a clear valid/invalid verdict showing only
            the disclosed claim. Screen at scale with the metered verification API.
          </p>
          <div className="flex flex-wrap gap-3 mt-7">
            <a href={APP_URL}><Button variant="primary" iconRight={IconArrowRight}>Open the verifier</Button></a>
            <a href={APP_URL}><Button variant="ghost" icon={IconKey}>Get API access</Button></a>
          </div>
        </div>
        <div className="bg-charcoal guilloche p-9 lg:p-11 flex flex-col justify-center">
          <div className="rounded-2xl border border-success/25 overflow-hidden bg-white">
            <div className="bg-success/10 px-5 py-4 flex items-center gap-3">
              <span className="w-10 h-10 rounded-full bg-success text-white flex items-center justify-center"><IconCheck size={22} stroke={2.6} /></span>
              <div>
                <div className="text-[15px] font-medium text-success">Valid credential</div>
                <div className="text-[12px] text-charcoal/65">Attested by Investec</div>
              </div>
            </div>
            <div className="p-5">
              <div className="text-[11px] uppercase tracking-wide text-muted">Disclosed claim</div>
              <div className="mt-2.5 flex items-center gap-2.5 rounded-lg border border-line bg-white px-3.5 py-3">
                <IconCheckCircle size={18} className="text-success shrink-0" />
                <span className="text-[14px] font-medium text-charcoal">Average monthly inflow ≥ R30,000</span>
              </div>
              <div className="mt-3 flex items-start gap-2 text-[12px] text-muted leading-relaxed">
                <IconLock size={13} className="shrink-0 mt-0.5" /> Exact amounts and all other claims are not disclosed.
              </div>
            </div>
          </div>
          <div className="mt-4 rounded-xl bg-black/30 border border-white/10 p-3.5 font-mono text-[11.5px] leading-relaxed text-white/80">
            <div><span className="text-brand">POST</span> /v1/verify</div>
            <div className="text-success mt-1">→ 200 · {'{ valid: true, claims: [...] }'}</div>
          </div>
        </div>
      </div>
    </section>
  );
}

/* ---------------- CTA ---------------- */
function CTA() {
  return (
    <section className="max-w-[1180px] mx-auto px-6 pb-24">
      <div className="reveal relative rounded-3xl bg-charcoal guilloche overflow-hidden text-center px-6 py-20">
        <div className="absolute inset-0 grid-dots opacity-[0.12] pointer-events-none" />
        <div className="relative">
          <TshepoMark size={44} tone="light" />
          <h2 className="text-[clamp(28px,3.6vw,42px)] leading-[1.08] tracking-tight mt-5 text-white max-w-[680px] mx-auto">
            Your bank already knows you’re good for it.<br />Now you can prove it — privately.
          </h2>
          <div className="flex flex-wrap items-center justify-center gap-3 mt-8">
            <a href={APP_URL}><Button variant="primary" size="lg" iconRight={IconArrowRight}>Connect your account</Button></a>
            <a href={APP_URL}><Button variant="ghost" size="lg" className="bg-transparent text-white border-white/20 hover:border-white/40 hover:bg-white/5">Explore the demo</Button></a>
          </div>
          <div className="text-[12.5px] text-white/45 mt-6">Tshepo means “trust” in Sesotho and Setswana.</div>
        </div>
      </div>
    </section>
  );
}

/* ---------------- FOOTER ---------------- */
function Footer() {
  const cols = [
    { h: 'Product', links: ['How it works', 'The credential', 'Privacy', 'Pricing'] },
    { h: 'Verifiers', links: ['Open verifier', 'API docs', 'Pricing', 'Status'] },
    { h: 'Company', links: ['About', 'Security', 'Contact', 'Careers'] },
  ];
  return (
    <footer className="border-t border-line bg-white">
      <div className="max-w-[1180px] mx-auto px-6 py-14 grid md:grid-cols-[1.4fr_1fr_1fr_1fr] gap-10">
        <div>
          <TshepoWordmark tone="dark" />
          <p className="text-[13.5px] text-muted leading-relaxed mt-4 max-w-[260px]">Bank-attested proof of funds, built on Investec Programmable Banking. Prove what’s true without revealing everything true.</p>
          <div className="flex items-center gap-2 mt-5 text-[12px] text-muted"><IconLock size={14} className="text-success" /> Bank-grade encryption</div>
        </div>
        {cols.map((c) => (
          <div key={c.h}>
            <div className="text-[13px] font-medium text-charcoal">{c.h}</div>
            <ul className="mt-3.5 space-y-2.5">
              {c.links.map((l) => (
                <li key={l}><a href={APP_URL} className="text-[13.5px] text-muted hover:text-charcoal transition-colors">{l}</a></li>
              ))}
            </ul>
          </div>
        ))}
      </div>
      <div className="border-t border-line">
        <div className="max-w-[1180px] mx-auto px-6 py-5 flex flex-wrap items-center justify-between gap-3 text-[12.5px] text-muted">
          <span>© 2026 Tshepo. A proof-of-funds credential issuer.</span>
          <div className="flex items-center gap-5">
            <a href="#" className="hover:text-charcoal transition-colors">Privacy</a>
            <a href="#" className="hover:text-charcoal transition-colors">Terms</a>
            <a href="#" className="hover:text-charcoal transition-colors">Security</a>
          </div>
        </div>
      </div>
    </footer>
  );
}

/* ---------------- PAGE ---------------- */
function Landing() {
  useReveal();
  return (
    <div className="min-h-screen bg-page">
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

ReactDOM.createRoot(document.getElementById('root')).render(<Landing />);
