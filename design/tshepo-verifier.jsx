/* Tshepo — standalone public Verifier page (no holder login). */

function VerifierPage({ lastPresentation }) {
  const [input, setInput] = useState('');
  const [result, setResult] = useState(null); // { valid, presentation }
  const [verifying, setVerifying] = useState(false);

  const sampleToken = lastPresentation?.token || 'tshpo_pres_8f3a91c4d2e07b65a1f4c93e2db8077a5c1e6f40';

  const verify = () => {
    const text = input.trim();
    if (!text) return;
    setVerifying(true);
    setResult(null);
    setTimeout(() => {
      const matches = lastPresentation && text === lastPresentation.token;
      const looksValid = /^tshpo_pres_[0-9a-f]{8,}$/.test(text);
      if (matches || looksValid) {
        const pres = matches ? lastPresentation : {
          credTitle: 'Proof of income — rental application',
          revealedIds: ['inflow30'],
          expiry: '12 Sep 2026',
        };
        setResult({ valid: true, pres });
      } else {
        setResult({ valid: false });
      }
      setVerifying(false);
    }, 950);
  };

  return (
    <div className="min-h-full">
      {/* public header */}
      <header className="bg-white border-b border-line">
        <div className="max-w-[1080px] mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <TshepoMark size={24} tone="brand" />
            <div className="leading-none">
              <span className="text-[18px] tracking-tight text-charcoal">tshepo</span>
              <span className="text-[18px] tracking-tight text-muted"> verify</span>
            </div>
          </div>
          <div className="flex items-center gap-3">
            <span className="hidden sm:flex items-center gap-1.5 text-[12.5px] text-muted"><IconShieldCheck size={14} className="text-success" /> Public verification service</span>
            <Button variant="ghost" size="sm" icon={IconKey}>Get API access</Button>
          </div>
        </div>
      </header>

      <div className="max-w-[1080px] mx-auto px-6 py-12">
        <div className="max-w-[560px]">
          <Pill tone="brand" icon={IconShield}>Trusted by Investec attestation</Pill>
          <h1 className="text-[30px] leading-[1.15] tracking-tight text-charcoal mt-4">Verify a proof-of-funds credential</h1>
          <p className="text-[15px] text-muted leading-relaxed mt-3">
            Paste a presentation token from an applicant. You’ll see a clear verdict and only
            the claims they chose to disclose — never their account or transactions.
          </p>
        </div>

        <div className="grid lg:grid-cols-[1fr_400px] gap-7 mt-9 items-start">
          {/* input */}
          <div className="rounded-2xl bg-white border border-line shadow-card p-6">
            <label className="text-[13px] font-medium text-charcoal">Presentation token</label>
            <textarea value={input} onChange={(e) => setInput(e.target.value)} rows={4}
              placeholder="Paste the token shared by the applicant…"
              className="mt-2 w-full px-4 py-3 rounded-xl border border-line bg-page/40 text-[13.5px] font-mono outline-none resize-none focus:border-brand focus:ring-4 focus:ring-brand/10 transition-all" />
            <div className="flex items-center justify-between gap-3 mt-3">
              <button onClick={() => setInput(sampleToken)} className="text-[12.5px] text-brand hover:text-brandhover transition-colors flex items-center gap-1.5">
                <IconSparkle size={14} /> {lastPresentation ? 'Use the token from your last presentation' : 'Paste a sample token'}
              </button>
              <Button variant="primary" icon={verifying ? null : IconShieldCheck} disabled={!input.trim() || verifying} onClick={verify}>
                {verifying ? 'Verifying…' : 'Verify'}
              </Button>
            </div>

            {/* result */}
            {result && (
              <div className="mt-5 anim-in">
                {result.valid ? (
                  <div className="rounded-xl border border-success/25 overflow-hidden">
                    <div className="bg-success/10 px-5 py-4 flex items-center gap-3">
                      <span className="w-10 h-10 rounded-full bg-success text-white flex items-center justify-center"><IconCheck size={22} stroke={2.6} /></span>
                      <div>
                        <div className="text-[16px] font-medium text-success">Valid credential</div>
                        <div className="text-[12.5px] text-charcoal/70">Cryptographically attested by Investec</div>
                      </div>
                    </div>
                    <div className="p-5">
                      <div className="text-[12px] text-muted uppercase tracking-wide">Disclosed claims</div>
                      <div className="mt-2.5 space-y-2">
                        {result.pres.revealedIds.map((id) => (
                          <div key={id} className="flex items-center gap-2.5 rounded-lg border border-line bg-white px-3.5 py-3">
                            <IconCheckCircle size={18} className="text-success shrink-0" />
                            <span className="text-[14px] font-medium text-charcoal">{claimById(id)?.label}</span>
                          </div>
                        ))}
                      </div>
                      <div className="mt-3.5 flex items-start gap-2 text-[12.5px] text-muted leading-relaxed">
                        <IconLock size={14} className="text-muted shrink-0 mt-0.5" />
                        Exact amounts and all other claims are not disclosed. The applicant revealed only what you see above.
                      </div>
                      <div className="mt-4 pt-4 border-t border-line grid grid-cols-3 gap-2 text-[12.5px]">
                        <div><div className="text-muted">Holder</div><div className="text-charcoal font-medium mt-0.5">{HOLDER.did}</div></div>
                        <div><div className="text-muted">Issuer</div><div className="text-charcoal font-medium mt-0.5">Investec</div></div>
                        <div><div className="text-muted">Valid until</div><div className="text-charcoal font-medium mt-0.5">{result.pres.expiry}</div></div>
                      </div>
                    </div>
                  </div>
                ) : (
                  <div className="rounded-xl border border-warn/30 bg-warn/[0.07] px-5 py-4 flex items-center gap-3">
                    <span className="w-10 h-10 rounded-full bg-warn text-white flex items-center justify-center"><IconX size={20} stroke={2.6} /></span>
                    <div>
                      <div className="text-[16px] font-medium text-warn">Could not verify</div>
                      <div className="text-[12.5px] text-charcoal/70">This token is malformed, expired, or revoked. Ask the applicant to generate a fresh presentation.</div>
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>

          {/* side: how it works + API marketing */}
          <div className="space-y-5">
            <div className="rounded-2xl bg-white border border-line shadow-card p-6">
              <div className="text-[14px] font-medium text-charcoal">How verification works</div>
              <div className="mt-4 space-y-4">
                <Assurance icon={IconScan} title="Paste & check">Validate a presentation in seconds, no account needed.</Assurance>
                <Assurance icon={IconEye} title="See only what’s shared">Disclosed claims are shown in full; everything else stays hidden.</Assurance>
                <Assurance icon={IconShieldCheck} title="Bank-attested">Every claim is signed by Investec — you’re trusting the bank, not a PDF.</Assurance>
              </div>
            </div>

            <div className="rounded-2xl bg-charcoal guilloche text-white p-6">
              <Pill tone="brand" icon={IconKey} className="bg-brand/15 border-brand/25 text-brand">Verification API</Pill>
              <div className="text-[16px] font-medium mt-3">Verify at scale</div>
              <p className="text-[13px] text-white/60 leading-relaxed mt-1.5">Screen applicants automatically. Metered, pay-as-you-go.</p>
              <div className="mt-4 rounded-xl bg-black/30 border border-white/10 p-3.5 font-mono text-[11.5px] leading-relaxed">
                <div><span className="text-brand">POST</span> <span className="text-white/80">/v1/verify</span></div>
                <div className="text-white/40 mt-1.5">{'{ "token": "tshpo_pres_…" }'}</div>
                <div className="text-success mt-1.5">→ 200 · {'{ valid: true, claims: [...] }'}</div>
              </div>
              <Button variant="primary" size="md" icon={IconKey} className="w-full mt-4">Get an API key</Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

Object.assign(window, { VerifierPage });
