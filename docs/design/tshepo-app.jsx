/* Tshepo — app shell, demo surface switch, routing, mount. */

function DemoBar({ surface, setSurface }) {
  return (
    <div className="bg-[#121212] text-white/80">
      <div className="max-w-[1180px] mx-auto px-6 h-11 flex items-center justify-between">
        <a href="landing.html" className="flex items-center gap-2.5 text-[12px] text-white/45 hover:text-white/80 transition-colors group">
          <IconArrowLeft size={13} className="text-brand" />
          <span className="hidden sm:inline">Back to tshepo.com</span>
          <span className="sm:hidden">Home</span>
        </a>
        <div className="inline-flex items-center gap-1 bg-white/[0.06] rounded-lg p-1 border border-white/10">
          {[
            { v: 'holder', label: 'Holder app', icon: IconUser },
            { v: 'verifier', label: 'Verifier page', icon: IconScan },
          ].map((o) => {
            const active = surface === o.v;
            return (
              <button key={o.v} onClick={() => setSurface(o.v)}
                className={`flex items-center gap-2 h-8 px-3 rounded-md text-[12.5px] font-medium transition-all duration-200 whitespace-nowrap
                  ${active ? 'bg-white text-charcoal' : 'text-white/60 hover:text-white'}`}>
                <o.icon size={14} />{o.label}
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
}

function HolderHeader({ connected, onHome }) {
  return (
    <header className="bg-charcoal">
      <div className="max-w-[1180px] mx-auto px-6 h-16 flex items-center justify-between">
        <button onClick={onHome} className="transition-opacity hover:opacity-80"><TshepoWordmark tone="light" sub="Proof of funds" /></button>
        <div className="flex items-center gap-4">
          {connected && (
            <span className="hidden md:flex items-center gap-2 text-[12.5px] text-white/55 bg-white/[0.06] rounded-full pl-2.5 pr-3 h-8 border border-white/10 whitespace-nowrap shrink-0">
              <Dot tone="success" /> {HOLDER.bank}
            </span>
          )}
          <div className="flex items-center gap-2.5">
            <span className="w-9 h-9 rounded-full bg-brand/20 text-brand flex items-center justify-center text-[13px] font-medium border border-brand/30">{HOLDER.initials}</span>
            <div className="leading-none hidden sm:block">
              <div className="text-white text-[13.5px]">{HOLDER.name}</div>
              <div className="text-white/45 text-[11.5px] mt-0.5">{HOLDER.accountMasked}</div>
            </div>
          </div>
        </div>
      </div>
    </header>
  );
}

function HolderApp() {
  const [connected, setConnected] = useState(false);
  const [view, setView] = useState('connect'); // connect | hub | issue | present
  const [credentials, setCredentials] = useState(INITIAL_CREDENTIALS);
  const [cardStyle, setCardStyle] = useState('passport');
  const [presentCred, setPresentCred] = useState(null);
  const [toast, setToast] = useState(null);

  const flash = (msg) => { setToast(msg); setTimeout(() => setToast(null), 2400); };

  const goHome = () => { if (connected) setView('hub'); };

  return (
    <div className="min-h-full flex flex-col">
      <HolderHeader connected={connected} onHome={goHome} />
      <main className="flex-1">
        {view === 'connect' && (
          <ConnectBank onConnected={() => { setConnected(true); setView('hub'); }} />
        )}
        {view === 'hub' && (
          <Hub credentials={credentials} cardStyle={cardStyle} setCardStyle={setCardStyle}
            onIssue={() => setView('issue')}
            onPresent={(c) => { setPresentCred(c); setView('present'); }}
            onDownload={(c) => flash(`Downloaded “${c.title}” as SD-JWT`)} />
        )}
        {view === 'issue' && (
          <IssueCredential onBack={() => setView('hub')}
            onIssued={(cred) => { setCredentials((cs) => [cred, ...cs]); setView('hub'); flash('Credential issued and added to your wallet'); }} />
        )}
        {view === 'present' && presentCred && (
          <PresentFlow cred={presentCred} onBack={() => setView('hub')}
            onGenerated={(p) => { window.__tshepoLastPres = p; flash('Presentation generated'); }} />
        )}
      </main>

      {/* toast */}
      {toast && (
        <div className="fixed bottom-6 left-1/2 -translate-x-1/2 z-50 anim-in">
          <div className="flex items-center gap-2.5 bg-charcoal text-white rounded-xl pl-4 pr-5 h-12 shadow-lift text-[13.5px]">
            <IconCheckCircle size={18} className="text-success" /> {toast}
          </div>
        </div>
      )}
    </div>
  );
}

function App() {
  const [surface, setSurface] = useState('holder');
  // lastPresentation bridges holder → verifier for a cohesive demo
  const [lastPres, setLastPres] = useState(null);

  // Re-read the bridge each time we land on the verifier.
  useEffect(() => {
    if (surface === 'verifier' && window.__tshepoLastPres) setLastPres(window.__tshepoLastPres);
  }, [surface]);

  return (
    <div className="min-h-screen flex flex-col bg-page">
      <DemoBar surface={surface} setSurface={setSurface} />
      <div className="flex-1 flex flex-col">
        {surface === 'holder' ? <HolderApp /> : <VerifierPage lastPresentation={lastPres} />}
      </div>
    </div>
  );
}

ReactDOM.createRoot(document.getElementById('root')).render(<App />);
