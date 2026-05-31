/* Tshepo — credential card, two variations: 'passport' and 'minimal'. */

function ClaimChip({ id, masked = false }) {
  const c = claimById(id);
  if (!c) return null;
  return (
    <span className={`inline-flex items-center gap-1.5 h-[28px] px-2.5 rounded-lg text-[12.5px] font-medium border
      ${masked ? 'bg-page text-muted border-line' : 'bg-brand/[0.07] text-charcoal border-brand/15'}`}>
      <IconShieldCheck size={13} className={masked ? 'text-muted' : 'text-brand'} stroke={1.8} />
      {c.short}
    </span>
  );
}

/* ---- (1) Passport / ID style ---- */
function PassportCard({ cred, onPresent, onDownload }) {
  const meta = STATUS_META[cred.status];
  const expired = cred.status === 'expired';
  return (
    <div className={`anim-in rounded-2xl bg-white border border-line shadow-card overflow-hidden transition-all duration-200 hover:shadow-lift hover:-translate-y-[2px] ${expired ? 'opacity-[0.72]' : ''}`}>
      {/* brand header band */}
      <div className="relative bg-charcoal guilloche px-6 pt-5 pb-[18px]">
        <div className="flex items-start justify-between">
          <div className="flex items-center gap-2.5">
            <TshepoMark size={24} tone="light" />
            <div className="leading-none">
              <div className="text-white text-[14px]">tshepo</div>
              <div className="text-white/50 text-[10.5px] mt-1">Verifiable credential</div>
            </div>
          </div>
          <Pill tone={meta.tone} icon={meta.icon}>{meta.label}</Pill>
        </div>
        <div className="mt-5 flex items-end justify-between">
          <div>
            <div className="text-white/45 text-[11px]">Holder</div>
            <div className="text-white text-[20px] mt-1 tracking-tight">{HOLDER.name}</div>
          </div>
          <div className="text-right">
            <div className="text-white/45 text-[11px]">Attested by</div>
            <div className="text-white text-[13.5px] mt-1.5 flex items-center gap-1.5 justify-end">
              <IconBank size={14} className="text-brand" />Investec
            </div>
          </div>
        </div>
      </div>
      {/* body */}
      <div className="px-6 pt-[18px] pb-5">
        <div className="text-[15px] font-medium text-charcoal">{cred.title}</div>
        <div className="text-[13px] text-muted mt-0.5">{cred.purpose}</div>
        <div className="mt-3.5 flex flex-wrap gap-2">
          {cred.claimIds.map((id) => <ClaimChip key={id} id={id} />)}
        </div>
        <div className="mt-4 pt-4 border-t border-line grid grid-cols-3 gap-2 text-[12.5px]">
          <div>
            <div className="text-muted">Claims</div>
            <div className="text-charcoal font-medium mt-0.5">{cred.claimIds.length} attested</div>
          </div>
          <div>
            <div className="text-muted">Issued</div>
            <div className="text-charcoal font-medium mt-0.5">{cred.issued}</div>
          </div>
          <div>
            <div className="text-muted">Valid until</div>
            <div className={`font-medium mt-0.5 ${cred.status === 'expiring' ? 'text-warn' : expired ? 'text-muted' : 'text-charcoal'}`}>{cred.expiry}</div>
          </div>
        </div>
      </div>
      {/* actions */}
      <div className="px-6 pb-5 flex items-center gap-2.5">
        <Button variant="primary" size="md" icon={IconScan} className="flex-1" disabled={expired} onClick={() => onPresent(cred)}>Present</Button>
        <Button variant="ghost" size="md" icon={IconDownload} onClick={() => onDownload(cred)}>Download</Button>
      </div>
    </div>
  );
}

/* ---- (2) Minimal receipt style ---- */
function MinimalCard({ cred, onPresent, onDownload }) {
  const meta = STATUS_META[cred.status];
  const expired = cred.status === 'expired';
  return (
    <div className={`anim-in group rounded-2xl bg-white border border-line shadow-card px-5 py-[18px] transition-all duration-200 hover:shadow-lift hover:-translate-y-[2px] ${expired ? 'opacity-[0.72]' : ''}`}>
      <div className="flex items-start gap-4">
        <div className="shrink-0 w-11 h-11 rounded-xl bg-charcoal flex items-center justify-center">
          <TshepoMark size={22} tone="light" />
        </div>
        <div className="min-w-0 flex-1">
          <div className="flex items-center justify-between gap-3">
            <div className="text-[15px] font-medium text-charcoal truncate">{cred.title}</div>
            <Pill tone={meta.tone} icon={meta.icon} className="shrink-0">{meta.label}</Pill>
          </div>
          <div className="text-[12.5px] text-muted mt-1 flex flex-wrap items-center gap-x-2 gap-y-1">
            <span className="flex items-center gap-1.5 whitespace-nowrap"><IconBank size={13} />Investec</span>
            <span className="text-line">·</span>
            <span className="whitespace-nowrap">{cred.claimIds.length} claims</span>
            <span className="text-line">·</span>
            <span className={`whitespace-nowrap ${cred.status === 'expiring' ? 'text-warn' : ''}`}>Valid until {cred.expiry}</span>
          </div>
          <div className="mt-3 flex flex-wrap gap-1.5">
            {cred.claimIds.map((id) => <ClaimChip key={id} id={id} />)}
          </div>
        </div>
      </div>
      <div className="mt-4 pt-3.5 border-t border-line flex items-center gap-2 justify-end">
        <Button variant="quiet" size="sm" icon={IconDownload} onClick={() => onDownload(cred)}>Download</Button>
        <Button variant="primary" size="sm" icon={IconScan} disabled={expired} onClick={() => onPresent(cred)}>Present</Button>
      </div>
    </div>
  );
}

function CredentialCard({ cred, style = 'passport', onPresent, onDownload }) {
  return style === 'minimal'
    ? <MinimalCard cred={cred} onPresent={onPresent} onDownload={onDownload} />
    : <PassportCard cred={cred} onPresent={onPresent} onDownload={onDownload} />;
}

Object.assign(window, { ClaimChip, PassportCard, MinimalCard, CredentialCard });
