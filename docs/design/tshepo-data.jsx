/* Tshepo — sample data (realistic South African banking context). */

const HOLDER = {
  name: 'Thabo Mokoena',
  initials: 'TM',
  did: 'did:tshepo:5f2a·91c4',
  bank: 'Investec Private Bank',
  accountType: 'Private Bank Account',
  accountMasked: '•••• 4821',
};

/* Claims catalogue — each fact is computed server-side from a read-only data pull.
   `met` = whether the holder currently satisfies the threshold.
   `evidence` is shown to the holder only; it never enters the credential. */
const CLAIM_CATALOGUE = [
  { id: 'inflow30', label: 'Average monthly inflow ≥ R30,000', short: 'Inflow ≥ R30k',
    detail: 'Mean of net credits over the last 6 statement months.', met: true, evidence: 'R42,300 average', basis: 'Last 6 months' },
  { id: 'salary6', label: 'Recurring salary deposits ≥ 6 months', short: 'Salaried 6m+',
    detail: 'A consistent monthly credit from the same payer.', met: true, evidence: '8 consecutive months', basis: 'PattersonGroup (Pty) Ltd' },
  { id: 'balance15', label: 'Average balance ≥ R15,000', short: 'Balance ≥ R15k',
    detail: 'Mean closing balance across the period.', met: true, evidence: 'R28,650 average', basis: 'Last 6 months' },
  { id: 'noOverdraft', label: 'No overdraft in the last 12 months', short: 'No overdraft 12m',
    detail: 'No balance below zero across the period.', met: true, evidence: '0 overdraft events', basis: 'Last 12 months' },
  { id: 'acctAge24', label: 'Account open ≥ 24 months', short: 'Account 24m+',
    detail: 'Time since the account was opened.', met: true, evidence: '3 years 4 months', basis: 'Opened Jan 2023' },
  { id: 'inflow50', label: 'Average monthly inflow ≥ R50,000', short: 'Inflow ≥ R50k',
    detail: 'Mean of net credits over the last 6 statement months.', met: false, evidence: 'R42,300 average', basis: 'Last 6 months' },
];

const claimById = (id) => CLAIM_CATALOGUE.find((c) => c.id === id);

/* Credentials the holder already holds. status derived from dates at render. */
const INITIAL_CREDENTIALS = [
  {
    id: 'vc_rental_01',
    title: 'Proof of income — rental application',
    purpose: 'For a residential lease',
    issued: '12 Mar 2026',
    expiry: '12 Sep 2026',
    daysLeft: 106,
    status: 'active',
    issuer: 'Investec',
    vcRef: 'urn:vc:tshepo:8f3a-1d92',
    claimIds: ['inflow30', 'salary6', 'noOverdraft'],
  },
  {
    id: 'vc_vehicle_01',
    title: 'Affordability — vehicle finance',
    purpose: 'For a dealer finance application',
    issued: '02 Apr 2026',
    expiry: '02 Jul 2026',
    daysLeft: 34,
    status: 'expiring',
    issuer: 'Investec',
    vcRef: 'urn:vc:tshepo:b71c-44e0',
    claimIds: ['inflow30', 'balance15', 'acctAge24', 'noOverdraft'],
  },
  {
    id: 'vc_visa_01',
    title: 'Proof of funds — visa application',
    purpose: 'For a Schengen visa',
    issued: '18 Nov 2025',
    expiry: '18 Jan 2026',
    daysLeft: -131,
    status: 'expired',
    issuer: 'Investec',
    vcRef: 'urn:vc:tshepo:2a6f-9c11',
    claimIds: ['balance15', 'acctAge24'],
  },
];

const STATUS_META = {
  active: { tone: 'success', label: 'Active', icon: IconCheckCircle },
  expiring: { tone: 'warn', label: 'Expiring soon', icon: IconClock },
  expired: { tone: 'expired', label: 'Expired', icon: IconAlert },
};

const randToken = (prefix = 'tshpo_pres') => {
  const hex = '0123456789abcdef';
  let s = '';
  for (let i = 0; i < 40; i++) s += hex[Math.floor(Math.random() * 16)];
  return `${prefix}_${s}`;
};

Object.assign(window, {
  HOLDER, CLAIM_CATALOGUE, claimById, INITIAL_CREDENTIALS, STATUS_META, randToken,
});
