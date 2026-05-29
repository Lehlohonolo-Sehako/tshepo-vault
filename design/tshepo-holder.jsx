/* Tshepo — holder app screens: Connect bank, Hub, Issue, Present. */
const { useState, useEffect, useMemo } = React;

/* ============ A. CONNECT BANK ============ */
function ConnectBank({ onConnected }) {
  const [phase, setPhase] = useState('idle'); // idle | connecting | done
  const steps = ['Authorising read-only access', 'Pulling 12 months of statements', 'Computing claims in memory', 'Discarding raw transactions'];
  const [stepIdx, setStepIdx] = useState(0);

  useEffect(() => {
    if (phase !== 'connecting') return;
    if (stepIdx < steps.length) {
      const t = setTimeout(() => setStepIdx((i) => i + 1), 620);
      return () => clearTimeout(t);
    }
    const t = setTimeout(() => { setPhase('done'); setTimeout(onConnected, 760); }, 500);
    return () => clearTimeout(t);
  }, [phase, stepIdx]);

  return (
    <div className="max-w-[1040px] mx-auto px-6 py-12 anim-in">
      <div className="grid lg:grid-cols-[1.05fr_0.95fr] gap-7 items-stretch">
        {/* left — value + reassurance */}
        <div>
          <Pill tone="brand" icon={IconLock}>Privacy by design</Pill>
          <h1 className="text-[34px] leading-[1.12] tracking-tight mt-4 text-charcoal">
            Prove your finances<br />without revealing them.
          </h1>
          <p className="text-[15.5px] text-muted leading-relaxed mt-3.5 max-w-[440px]">
            Connect your Investec account once. Tshepo certifies the facts a landlord or lender
            needs — like “average monthly inflow ≥ R30,000” — and nothing else.
          </p>
          <div className="mt-8 space-y-5 max-w-[440px]">
            <Assurance icon={IconEye} title="Read-only access">
              Tshepo can read your statements to compute claims. It can never move money or change anything.
            </Assurance>
            <Assurance icon={IconShieldCheck} title="Raw data is never stored">
              Transactions are processed in memory to compute claims, then discarded. We keep the conclusions, not your spending.
            </Assurance>
            <Assurance icon={IconFingerprint} title="You decide what’s shared">
              Each time you present a credential, you choose exactly which claims to reveal.
            </Assurance>
          </div>
        </div>

        {/* right — connect card */}
        <div className="rounded-2xl bg-white border border-line shadow-card p-7 flex flex-col">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 rounded-xl bg-charcoal flex items-center justify-center"><IconBank size={22} className="text-brand" /></div>
            <div>
              <div className="text-[15px] font-medium text-charcoal">Investec Programmable Banking</div>
              <div className="text-[13px] text-muted">Secure OAuth connection</div>
            </div>
          </div>

          <div className="mt-6 rounded-xl border border-line bg-page/60 p-4 space-y-3">
            {steps.map((s, i) => {
              const active = phase === 'connecting' && i === stepIdx;
              const complete = (phase === 'connecting' && i < stepIdx) || phase === 'done';
              return (
                <div key={i} className="flex items-center gap-3 text-[13.5px]">
                  <span className={`flex items-center justify-center w-[22px] h-[22px] rounded-full transition-all duration-200
                    ${complete ? 'bg-success text-white' : active ? 'bg-brand/15 text-brand' : 'bg-white border border-line text-line'}`}>
                    {complete ? <IconCheck size={13} stroke={2.6} /> : active ? <span className="w-2 h-2 rounded-full bg-brand animate-pulse" /> : <span className="w-2 h-2 rounded-full bg-[#D7D7D4]" />}
                  </span>
                  <span className={complete || active ? 'text-charcoal' : 'text-muted'}>{s}</span>
                </div>
              );
            })}
          </div>

          <div className="mt-6 flex-1 flex flex-col justify-end">
            {phase === 'done' ? (
              <div className="flex items-center justify-center gap-2 h-[52px] text-success font-medium text-[15px]">
                <IconCheckCircle size={20} /> Account connected
              </div>
            ) : (
              <Button variant="dark" size="lg" icon={IconLink} disabled={phase === 'connecting'}
                onClick={() => { setPhase('connecting'); setStepIdx(0); }} className="w-full">
                {phase === 'connecting' ? 'Connecting…' : 'Connect Investec account'}
              </Button>
            )}
            <div className="flex items-center justify-center gap-1.5 text-[12px] text-muted mt-3.5">
              <IconLock size={13} /> Bank-grade encryption · you can disconnect anytime
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

/* ============ B. HUB — my credentials ============ */
function Hub({ credentials, cardStyle, setCardStyle, onIssue, onPresent, onDownload }) {
  const active = credentials.filter((c) => c.status !== 'expired');
  return (
    <div className="max-w-[1100px] mx-auto px-6 py-10 anim-in">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <div className="text-[14px] text-muted">Welcome back, {HOLDER.name.split(' ')[0]}</div>
          <h1 className="text-[27px] tracking-tight text-charcoal mt-1 whitespace-nowrap">My credentials</h1>
        </div>
        <Button variant="primary" icon={IconPlus} onClick={onIssue}>Issue new credential</Button>
      </div>

      {/* connected bank strip */}
      <div className="mt-6 flex flex-wrap items-center justify-between gap-3 rounded-xl bg-white border border-line shadow-softer px-4 py-3">
        <div className="flex items-center gap-3 text-[13.5px]">
          <span className="w-9 h-9 rounded-lg bg-charcoal flex items-center justify-center"><IconBank size={17} className="text-brand" /></span>
          <div>
            <div className="text-charcoal font-medium flex items-center gap-2">{HOLDER.bank} <Dot tone="success" /></div>
            <div className="text-muted text-[12.5px]">{HOLDER.accountType} · {HOLDER.accountMasked} · read-only</div>
          </div>
        </div>
        <div className="flex items-center gap-3">
          <span className="text-[12.5px] text-muted hidden sm:flex items-center gap-1.5"><IconShieldCheck size={14} className="text-success" /> Raw data not stored</span>
          <div className="flex items-center gap-2">
            <span className="text-[12.5px] text-muted mr-0.5">Card style</span>
            <Segmented size="sm" value={cardStyle} onChange={setCardStyle}
              options={[{ value: 'passport', label: 'Passport' }, { value: 'minimal', label: 'Minimal' }]} />
          </div>
        </div>
      </div>

      {/* grid */}
      {credentials.length === 0 ? (
        <div className="mt-8 rounded-2xl border border-dashed border-line bg-white/60 py-16 text-center">
          <div className="w-14 h-14 rounded-2xl bg-brand/10 text-brand flex items-center justify-center mx-auto"><IconShield size={26} /></div>
          <div className="text-[16px] font-medium text-charcoal mt-4">No credentials yet</div>
          <div className="text-[13.5px] text-muted mt-1">Issue your first proof-of-funds credential.</div>
          <Button variant="primary" icon={IconPlus} className="mt-5 mx-auto" onClick={onIssue}>Issue new credential</Button>
        </div>
      ) : (
        <div className={`mt-6 grid gap-5 ${cardStyle === 'passport' ? 'md:grid-cols-2' : 'grid-cols-1 max-w-[760px]'}`}>
          {credentials.map((c) => (
            <CredentialCard key={c.id} cred={c} style={cardStyle} onPresent={onPresent} onDownload={onDownload} />
          ))}
        </div>
      )}
    </div>
  );
}

/* ============ C. ISSUE CREDENTIAL ============ */
function IssueCredential({ onBack, onIssued }) {
  const [selected, setSelected] = useState(() => new Set(['inflow30', 'salary6', 'noOverdraft']));
  const [title, setTitle] = useState('Proof of income — rental application');
  const [issuing, setIssuing] = useState(false);
  const toggle = (id) => setSelected((s) => { const n = new Set(s); n.has(id) ? n.delete(id) : n.add(id); return n; });

  const doIssue = () => {
    setIssuing(true);
    setTimeout(() => {
      const cred = {
        id: 'vc_' + Math.random().toString(36).slice(2, 8),
        title: title || 'Proof of funds',
        purpose: 'Issued just now',
        issued: '29 May 2026', expiry: '29 Nov 2026', daysLeft: 184,
        status: 'active', issuer: 'Investec',
        vcRef: 'urn:vc:tshepo:' + Math.random().toString(16).slice(2, 6) + '-' + Math.random().toString(16).slice(2, 6),
        claimIds: [...selected],
      };
      onIssued(cred);
    }, 1100);
  };

  return (
    <div className="max-w-[760px] mx-auto px-6 py-9 anim-in">
      <button onClick={onBack} className="flex items-center gap-2 text-[13.5px] text-muted hover:text-charcoal transition-colors"><IconArrowLeft size={16} /> Back to my credentials</button>
      <h1 className="text-[26px] tracking-tight text-charcoal mt-4">Issue a new credential</h1>
      <p className="text-[14.5px] text-muted mt-1.5">Choose the facts to certify. Each is checked against your account in real time — Tshepo only certifies what is currently true.</p>

      {/* title field */}
      <div className="mt-7">
        <label className="text-[13px] font-medium text-charcoal">Credential name</label>
        <input value={title} onChange={(e) => setTitle(e.target.value)}
          className="mt-2 w-full h-12 px-4 rounded-xl border border-line bg-white text-[14.5px] outline-none focus:border-brand focus:ring-4 focus:ring-brand/10 transition-all" />
      </div>

      {/* claims */}
      <div className="mt-6">
        <div className="text-[13px] font-medium text-charcoal mb-2.5">Claims to certify</div>
        <div className="space-y-2.5">
          {CLAIM_CATALOGUE.map((c) => {
            const checked = selected.has(c.id);
            const disabled = !c.met;
            return (
              <button key={c.id} disabled={disabled} onClick={() => toggle(c.id)}
                className={`w-full text-left flex items-start gap-3.5 rounded-xl border p-4 transition-all duration-200
                  ${disabled ? 'bg-page/60 border-line cursor-not-allowed' : checked ? 'bg-white border-brand/40 ring-4 ring-brand/[0.07] shadow-softer' : 'bg-white border-line hover:border-charcoal/20'}`}>
                <span className="mt-0.5"><Checkbox checked={checked} disabled={disabled} /></span>
                <span className="flex-1 min-w-0">
                  <span className="flex items-center justify-between gap-3">
                    <span className={`text-[14.5px] font-medium leading-snug ${disabled ? 'text-muted' : 'text-charcoal'}`}>{c.label}</span>
                    {c.met
                      ? <Pill tone="success" icon={IconCheck}>Met</Pill>
                      : <Pill tone="warn" icon={IconX}>Not met</Pill>}
                  </span>
                  <span className="block text-[13px] text-muted mt-1.5 leading-relaxed">{c.detail}</span>
                  <span className="block text-[12.5px] mt-1.5">
                    <span className={disabled ? 'text-warn' : 'text-success'}>{c.evidence}</span>
                    <span className="text-muted"> · {c.basis}</span>
                  </span>
                </span>
              </button>
            );
          })}
        </div>
      </div>

      {/* privacy note */}
      <div className="mt-5 flex gap-3 rounded-xl bg-brand/[0.06] border border-brand/15 p-4">
        <IconLock size={18} className="text-brand shrink-0 mt-0.5" />
        <div className="text-[13px] text-charcoal/80 leading-relaxed">
          The credential certifies the <span className="font-medium text-charcoal">threshold is met</span> — never the exact figure.
          A verifier sees “inflow ≥ R30,000”, not “R42,300”.
        </div>
      </div>

      {/* footer */}
      <div className="mt-6 flex items-center justify-between gap-4 sticky bottom-4">
        <div className="text-[13.5px] text-muted">{selected.size} claim{selected.size === 1 ? '' : 's'} selected</div>
        <Button variant="primary" size="lg" icon={issuing ? null : IconShieldCheck} disabled={selected.size === 0 || issuing} onClick={doIssue}>
          {issuing ? 'Issuing…' : 'Issue credential'}
        </Button>
      </div>
    </div>
  );
}

/* ============ D. PRESENT — selective disclosure ============ */
function PresentFlow({ cred, onBack, onGenerated }) {
  const [reveal, setReveal] = useState(() => new Set([cred.claimIds[0]]));
  const [token, setToken] = useState(null);
  const [copied, setCopied] = useState(false);
  const toggle = (id) => setReveal((s) => { const n = new Set(s); n.has(id) ? n.delete(id) : n.add(id); return n; });
  const revealedIds = cred.claimIds.filter((id) => reveal.has(id));

  const generate = () => {
    const t = randToken();
    setToken(t);
    onGenerated({ token: t, credId: cred.id, credTitle: cred.title, revealedIds, expiry: cred.expiry });
  };
  const copy = () => { setCopied(true); setTimeout(() => setCopied(false), 1500); };

  return (
    <div className="max-w-[920px] mx-auto px-6 py-9 anim-in">
      <button onClick={onBack} className="flex items-center gap-2 text-[13.5px] text-muted hover:text-charcoal transition-colors"><IconArrowLeft size={16} /> Back to my credentials</button>
      <h1 className="text-[26px] tracking-tight text-charcoal mt-4">Present “{cred.title}”</h1>
      <p className="text-[14.5px] text-muted mt-1.5">Tick only the claims you want to reveal. Everything else stays cryptographically hidden — the verifier can’t even tell it exists.</p>

      <div className="grid lg:grid-cols-[1fr_360px] gap-7 mt-7 items-start">
        {/* picker */}
        <div className="space-y-2.5">
          {cred.claimIds.map((id) => {
            const c = claimById(id);
            const on = reveal.has(id);
            return (
              <div key={id} className={`flex items-center gap-3.5 rounded-xl border p-4 transition-all duration-200
                ${on ? 'bg-white border-brand/40 ring-4 ring-brand/[0.07]' : 'bg-page/50 border-line'}`}>
                <span className={`flex items-center justify-center w-9 h-9 rounded-lg shrink-0 transition-colors ${on ? 'bg-brand/12 text-brand' : 'bg-white border border-line text-muted'}`}>
                  {on ? <IconEye size={17} /> : <IconEyeOff size={17} />}
                </span>
                <div className="flex-1 min-w-0">
                  <div className={`text-[14px] font-medium ${on ? 'text-charcoal' : 'text-muted'}`}>{c.label}</div>
                  <div className="text-[12.5px] mt-0.5">{on ? <span className="text-brand">Revealed to verifier</span> : <span className="text-muted">Kept private</span>}</div>
                </div>
                <Toggle on={on} onChange={() => toggle(id)} />
              </div>
            );
          })}
          <div className="flex items-center gap-2 text-[12.5px] text-muted pt-1">
            <IconLock size={14} className="text-success" /> Exact amounts are never disclosed — only that each threshold is met.
          </div>
        </div>

        {/* live preview / output */}
        <div className="rounded-2xl bg-white border border-line shadow-card overflow-hidden lg:sticky lg:top-6">
          {!token ? (
            <>
              <div className="px-5 py-3.5 border-b border-line flex items-center gap-2 text-[13px] text-muted whitespace-nowrap">
                <IconScan size={15} /> What the verifier will see
              </div>
              <div className="p-5 space-y-2.5 bg-page/40 min-h-[210px]">
                {cred.claimIds.map((id) => {
                  const c = claimById(id); const on = reveal.has(id);
                  return (
                    <div key={id} className={`flex items-center gap-2.5 rounded-lg px-3 py-2.5 text-[12.5px] border ${on ? 'bg-white border-line' : 'bg-white/40 border-dashed border-line'}`}>
                      {on ? <IconCheckCircle size={15} className="text-success shrink-0" /> : <IconLock size={14} className="text-muted shrink-0" />}
                      <span className={on ? 'text-charcoal font-medium' : 'text-muted masked-text'}>{on ? c.label : c.label}</span>
                    </div>
                  );
                })}
              </div>
              <div className="p-5 border-t border-line">
                <div className="text-[12.5px] text-muted mb-3">{revealedIds.length} of {cred.claimIds.length} claims revealed</div>
                <Button variant="primary" size="md" icon={IconScan} className="w-full" disabled={revealedIds.length === 0} onClick={generate}>Generate presentation</Button>
              </div>
            </>
          ) : (
            <div className="p-6 anim-in">
              <div className="text-[13px] text-muted mb-3 flex items-center gap-2"><IconCheckCircle size={15} className="text-success" /> Presentation ready</div>
              <div className="rounded-xl border border-line p-4 flex items-center justify-center"><QrCode value={token} size={196} /></div>
              <div className="text-[12.5px] text-muted text-center mt-3">Scan, or share the token below</div>
              <div className="mt-3 flex items-center gap-2 rounded-xl border border-line bg-page/60 pl-3 pr-1.5 py-1.5">
                <code className="text-[12px] text-charcoal truncate flex-1">{token}</code>
                <Button variant="subtle" size="sm" icon={copied ? IconCheck : IconCopy} onClick={() => { navigator.clipboard?.writeText(token); copy(); }}>{copied ? 'Copied' : 'Copy'}</Button>
              </div>
              <div className="mt-4 pt-4 border-t border-line text-[12.5px] text-muted leading-relaxed">
                Reveals {revealedIds.length} claim{revealedIds.length === 1 ? '' : 's'}. Valid until {cred.expiry}. The verifier learns nothing beyond what you ticked.
              </div>
              <Button variant="ghost" size="md" className="w-full mt-4" onClick={() => setToken(null)}>Adjust disclosure</Button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

Object.assign(window, { ConnectBank, Hub, IssueCredential, PresentFlow });
