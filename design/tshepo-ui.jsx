/* Tshepo — shared UI primitives. */

function Button({ variant = 'primary', size = 'md', icon: Icon, iconRight: IconR, children, className = '', ...props }) {
  const base = 'inline-flex items-center justify-center gap-2 rounded-xl font-medium transition-all duration-200 select-none disabled:opacity-40 disabled:pointer-events-none whitespace-nowrap';
  const sizes = {
    sm: 'h-9 px-3.5 text-[13.5px]',
    md: 'h-11 px-5 text-[14.5px]',
    lg: 'h-[52px] px-6 text-[15.5px]',
  };
  const variants = {
    primary: 'bg-brand text-white hover:bg-brandhover shadow-soft hover:shadow-lift hover:-translate-y-[1px]',
    dark: 'bg-charcoal text-white hover:bg-surface hover:-translate-y-[1px] shadow-soft',
    ghost: 'bg-white text-charcoal border border-line hover:border-charcoal/25 hover:-translate-y-[1px] shadow-softer',
    subtle: 'bg-page text-charcoal hover:bg-line/70',
    quiet: 'text-muted hover:text-charcoal hover:bg-page',
    success: 'bg-success text-white hover:brightness-105 shadow-soft hover:-translate-y-[1px]',
  };
  return (
    <button className={`${base} ${sizes[size]} ${variants[variant]} ${className}`} {...props}>
      {Icon && <Icon size={size === 'lg' ? 19 : 17} />}
      {children}
      {IconR && <IconR size={size === 'lg' ? 19 : 17} />}
    </button>
  );
}

function Pill({ tone = 'neutral', icon: Icon, children, className = '' }) {
  const tones = {
    neutral: 'bg-page text-muted border-line',
    success: 'bg-success/10 text-success border-success/20',
    warn: 'bg-warn/10 text-warn border-warn/20',
    brand: 'bg-brand/10 text-brand border-brand/20',
    dark: 'bg-charcoal text-white border-transparent',
    expired: 'bg-[#E9E9E7] text-muted border-transparent',
  };
  return (
    <span className={`inline-flex items-center gap-1.5 h-[26px] px-2.5 rounded-full border text-[12.5px] font-medium whitespace-nowrap ${tones[tone]} ${className}`}>
      {Icon && <Icon size={13} stroke={1.9} />}{children}
    </span>
  );
}

function Dot({ tone = 'success' }) {
  const c = { success: 'bg-success', warn: 'bg-warn', brand: 'bg-brand', muted: 'bg-muted' }[tone];
  return <span className={`inline-block w-1.5 h-1.5 rounded-full ${c}`} />;
}

/* Soft segmented control */
function Segmented({ options, value, onChange, size = 'md' }) {
  const h = size === 'sm' ? 'h-9 text-[13px]' : 'h-11 text-[14px]';
  return (
    <div className={`inline-flex items-center bg-page rounded-xl p-1 border border-line ${h}`}>
      {options.map((o) => {
        const active = o.value === value;
        return (
          <button key={o.value} onClick={() => onChange(o.value)}
            className={`flex items-center gap-2 h-full px-3.5 rounded-lg font-medium transition-all duration-200
              ${active ? 'bg-white text-charcoal shadow-softer' : 'text-muted hover:text-charcoal'}`}>
            {o.icon && <o.icon size={15} />}{o.label}
          </button>
        );
      })}
    </div>
  );
}

/* Toggle switch */
function Toggle({ on, onChange, tone = 'brand' }) {
  const bg = on ? (tone === 'brand' ? 'bg-brand' : 'bg-success') : 'bg-[#D7D7D4]';
  return (
    <button onClick={() => onChange(!on)}
      className={`relative w-[42px] h-[24px] rounded-full transition-colors duration-200 shrink-0 ${bg}`}>
      <span className={`absolute top-[3px] left-[3px] w-[18px] h-[18px] bg-white rounded-full shadow-soft transition-transform duration-200 ${on ? 'translate-x-[18px]' : ''}`} />
    </button>
  );
}

function Checkbox({ checked, disabled }) {
  return (
    <span className={`flex items-center justify-center w-[22px] h-[22px] rounded-[7px] border transition-all duration-200 shrink-0
      ${checked ? 'bg-brand border-brand text-white' : disabled ? 'bg-page border-line' : 'bg-white border-[#CFCFCC]'}`}>
      {checked && <IconCheck size={14} stroke={2.4} />}
    </span>
  );
}

/* A reassurance row with soft icon chip */
function Assurance({ icon: Icon, title, children }) {
  return (
    <div className="flex gap-3.5">
      <div className="shrink-0 w-9 h-9 rounded-[11px] bg-brand/10 text-brand flex items-center justify-center mt-0.5">
        <Icon size={18} />
      </div>
      <div>
        <div className="text-[14.5px] font-medium text-charcoal">{title}</div>
        <div className="text-[13.5px] text-muted leading-relaxed mt-0.5">{children}</div>
      </div>
    </div>
  );
}

/* Deterministic QR-style placeholder (clearly a stand-in, premium soft look) */
function QrCode({ value = '', size = 188 }) {
  const n = 25, quiet = 1;
  let seed = 2166136261;
  for (let i = 0; i < value.length; i++) { seed ^= value.charCodeAt(i); seed = (seed * 16777619) >>> 0; }
  const rng = () => { seed = (seed * 1103515245 + 12345) & 0x7fffffff; return seed / 0x7fffffff; };
  const inFinder = (x, y) => {
    const f = (ox, oy) => x >= ox && x < ox + 7 && y >= oy && y < oy + 7;
    return f(0, 0) || f(n - 7, 0) || f(0, n - 7);
  };
  const cells = [];
  const m = size / (n + quiet * 2);
  for (let y = 0; y < n; y++) for (let x = 0; x < n; x++) {
    if (inFinder(x, y)) continue;
    if (rng() > 0.52) cells.push(<rect key={x + '-' + y} x={(x + quiet) * m} y={(y + quiet) * m} width={m * 0.92} height={m * 0.92} rx={m * 0.28} fill="#1A1A1A" />);
  }
  const Finder = ({ ox, oy }) => (
    <g>
      <rect x={(ox + quiet) * m} y={(oy + quiet) * m} width={m * 7} height={m * 7} rx={m * 1.6} fill="none" stroke="#1A1A1A" strokeWidth={m * 0.95} />
      <rect x={(ox + quiet + 2) * m} y={(oy + quiet + 2) * m} width={m * 3} height={m * 3} rx={m * 0.9} fill="#1A1A1A" />
    </g>
  );
  return (
    <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`} className="block">
      <rect width={size} height={size} rx="14" fill="#FFFFFF" />
      {cells}
      <Finder ox={0} oy={0} /><Finder ox={n - 7} oy={0} /><Finder ox={0} oy={n - 7} />
    </svg>
  );
}

Object.assign(window, { Button, Pill, Dot, Segmented, Toggle, Checkbox, Assurance, QrCode });
