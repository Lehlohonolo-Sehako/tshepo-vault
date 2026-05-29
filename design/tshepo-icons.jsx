/* Tshepo — icon set + brand mark. Minimal stroke icons, 1.6 weight, rounded. */
const Svg = ({ size = 20, children, className = '', stroke = 1.6, ...p }) => (
  <svg width={size} height={size} viewBox="0 0 24 24" fill="none"
    stroke="currentColor" strokeWidth={stroke} strokeLinecap="round" strokeLinejoin="round"
    className={className} {...p}>{children}</svg>
);

const IconShield = (p) => <Svg {...p}><path d="M12 3l7 3v5c0 4.5-3 7.6-7 9-4-1.4-7-4.5-7-9V6l7-3z" /></Svg>;
const IconShieldCheck = (p) => <Svg {...p}><path d="M12 3l7 3v5c0 4.5-3 7.6-7 9-4-1.4-7-4.5-7-9V6l7-3z" /><path d="M9 11.5l2 2 4-4" /></Svg>;
const IconLock = (p) => <Svg {...p}><rect x="5" y="11" width="14" height="9" rx="2.2" /><path d="M8 11V8a4 4 0 0 1 8 0v3" /></Svg>;
const IconCheck = (p) => <Svg {...p}><path d="M5 12.5l4 4 10-10" /></Svg>;
const IconCheckCircle = (p) => <Svg {...p}><circle cx="12" cy="12" r="9" /><path d="M8.5 12.5l2.5 2.5 5-5.5" /></Svg>;
const IconAlert = (p) => <Svg {...p}><circle cx="12" cy="12" r="9" /><path d="M12 8v5" /><path d="M12 16h.01" /></Svg>;
const IconX = (p) => <Svg {...p}><path d="M6 6l12 12M18 6L6 18" /></Svg>;
const IconXCircle = (p) => <Svg {...p}><circle cx="12" cy="12" r="9" /><path d="M9 9l6 6M15 9l-6 6" /></Svg>;
const IconEye = (p) => <Svg {...p}><path d="M2.5 12S6 5.5 12 5.5 21.5 12 21.5 12 18 18.5 12 18.5 2.5 12 2.5 12z" /><circle cx="12" cy="12" r="2.6" /></Svg>;
const IconEyeOff = (p) => <Svg {...p}><path d="M4 4l16 16" /><path d="M9.6 5.9A9.8 9.8 0 0 1 12 5.5c6 0 9.5 6.5 9.5 6.5a16 16 0 0 1-3 3.6" /><path d="M6.3 7.7A15.6 15.6 0 0 0 2.5 12S6 18.5 12 18.5c1 0 1.9-.2 2.8-.5" /><path d="M9.9 9.9a3 3 0 0 0 4.2 4.2" /></Svg>;
const IconPlus = (p) => <Svg {...p}><path d="M12 5v14M5 12h14" /></Svg>;
const IconDownload = (p) => <Svg {...p}><path d="M12 4v10" /><path d="M8 11l4 4 4-4" /><path d="M5 19h14" /></Svg>;
const IconArrowLeft = (p) => <Svg {...p}><path d="M11 6l-6 6 6 6" /><path d="M5 12h14" /></Svg>;
const IconArrowRight = (p) => <Svg {...p}><path d="M13 6l6 6-6 6" /><path d="M19 12H5" /></Svg>;
const IconChevron = (p) => <Svg {...p}><path d="M9 6l6 6-6 6" /></Svg>;
const IconChevronDown = (p) => <Svg {...p}><path d="M6 9l6 6 6-6" /></Svg>;
const IconCopy = (p) => <Svg {...p}><rect x="9" y="9" width="11" height="11" rx="2.2" /><path d="M5 15V6a2 2 0 0 1 2-2h9" /></Svg>;
const IconBank = (p) => <Svg {...p}><path d="M4 10l8-5 8 5" /><path d="M5 10v8M9.5 10v8M14.5 10v8M19 10v8" /><path d="M3 20h18" /></Svg>;
const IconLink = (p) => <Svg {...p}><path d="M9 15l6-6" /><path d="M10.5 6.5l1.2-1.2a3.5 3.5 0 0 1 5 5l-1.2 1.2" /><path d="M13.5 17.5l-1.2 1.2a3.5 3.5 0 0 1-5-5l1.2-1.2" /></Svg>;
const IconSparkle = (p) => <Svg {...p}><path d="M12 4l1.6 4.4L18 10l-4.4 1.6L12 16l-1.6-4.4L6 10l4.4-1.6L12 4z" /></Svg>;
const IconClock = (p) => <Svg {...p}><circle cx="12" cy="12" r="9" /><path d="M12 7.5V12l3 2" /></Svg>;
const IconKey = (p) => <Svg {...p}><circle cx="8" cy="14" r="3.5" /><path d="M10.5 11.5L20 2" /><path d="M16 6l2.5 2.5M14 8l2 2" /></Svg>;
const IconUser = (p) => <Svg {...p}><circle cx="12" cy="8.5" r="3.5" /><path d="M5.5 19a6.5 6.5 0 0 1 13 0" /></Svg>;
const IconScan = (p) => <Svg {...p}><path d="M4 8V6a2 2 0 0 1 2-2h2M16 4h2a2 2 0 0 1 2 2v2M20 16v2a2 2 0 0 1-2 2h-2M8 20H6a2 2 0 0 1-2-2v-2" /><path d="M4 12h16" /></Svg>;
const IconFingerprint = (p) => <Svg {...p}><path d="M12 5.5a6.5 6.5 0 0 1 6.5 6.5v1.5" /><path d="M5.5 14v-2a6.5 6.5 0 0 1 3-5.5" /><path d="M9 12a3 3 0 0 1 6 0v3a3 3 0 0 0 .5 1.6" /><path d="M12 12v4" /><path d="M8.6 16.5c.3 1 .8 1.8.8 1.8" /></Svg>;

/* Tshepo brand mark — a soft shield with an attested check node. */
function TshepoMark({ size = 28, tone = 'brand' }) {
  const c = tone === 'light' ? '#FFFFFF' : tone === 'dark' ? '#1A1A1A' : '#00A9E0';
  return (
    <svg width={size} height={size} viewBox="0 0 32 32" fill="none">
      <path d="M16 3.2l9 3.6v6.2c0 6-4 10.3-9 12-5-1.7-9-6-9-12V6.8l9-3.6z"
        fill={c} fillOpacity="0.10" stroke={c} strokeWidth="1.8" />
      <path d="M11.5 15.6l3 3 6-6.4" stroke={c} strokeWidth="1.9" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

function TshepoWordmark({ tone = 'dark', sub = false }) {
  const text = tone === 'light' ? 'text-white' : 'text-charcoal';
  return (
    <div className="flex items-center gap-2.5 select-none">
      <TshepoMark size={26} tone={tone === 'light' ? 'light' : 'brand'} />
      <div className="leading-none">
        <div className={`text-[19px] tracking-tight ${text}`}>tshepo</div>
        {sub && <div className={`text-[10.5px] mt-0.5 ${tone === 'light' ? 'text-white/55' : 'text-muted'}`}>{sub}</div>}
      </div>
    </div>
  );
}

Object.assign(window, {
  Svg, IconShield, IconShieldCheck, IconLock, IconCheck, IconCheckCircle, IconAlert, IconX, IconXCircle,
  IconEye, IconEyeOff, IconPlus, IconDownload, IconArrowLeft, IconArrowRight, IconChevron, IconChevronDown,
  IconCopy, IconBank, IconLink, IconSparkle, IconClock, IconKey, IconUser, IconScan, IconFingerprint,
  TshepoMark, TshepoWordmark,
});
