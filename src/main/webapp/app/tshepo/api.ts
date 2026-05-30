import axios from 'axios';

// --- Types matching OpenAPI VMs ---

export interface BankStatusResponse {
  connected: boolean;
  connectedAt?: string;
  maskedAccount?: string;
  status?: 'CONNECTED' | 'DISCONNECTED';
}

export interface ComputedClaim {
  type: string;
  operator: string;
  threshold: number;
  currency?: string;
  periodMonths?: number;
  met: boolean;
  basis?: string;
}

export interface ClaimThreshold {
  type: string;
  operator: string;
  threshold: number;
  currency?: string;
  periodMonths?: number;
}

export interface CredentialResponse {
  id: string;
  holderLogin?: string;
  title: string;
  purpose?: string;
  status?: 'ACTIVE' | 'REVOKED' | 'EXPIRED';
  issuedAt?: string;
  expiresAt?: string;
  issuerDid?: string;
  vcRef?: string;
  claims?: ComputedClaim[];
  claimCount?: number;
}

export interface CredentialListResponse {
  credentials: CredentialResponse[];
  total: number;
}

export interface CredentialIssueRequest {
  title: string;
  purpose?: string;
  claims: ClaimThreshold[];
  validityDays?: number;
}

export interface PresentRequest {
  disclosedClaimTypes: string[];
}

export interface PresentationResponse {
  token: string;
  qrDataUrl?: string;
  disclosedClaimTypes: string[];
  expiresAt?: string;
  credentialId: string;
}

export interface VerifyRequest {
  token: string;
}

export interface DisclosedClaim {
  type: string;
  operator: string;
  threshold: number;
  currency?: string;
  met: boolean;
}

export interface VerifyResponse {
  valid: boolean;
  issuerDid?: string;
  holderDid?: string;
  disclosedClaims?: DisclosedClaim[];
  expiresAt?: string;
  error?: string;
}

export interface CreateApiKeyRequest {
  label: string;
}

export interface ApiKeyResponse {
  id: string;
  label: string;
  active: boolean;
  callCount: number;
  createdAt?: string;
  rawKey?: string;
}

// --- API functions ---

export const bankApi = {
  connect: (code: string, redirectUri: string) =>
    axios.post<BankStatusResponse>('/api/bank/connect', { authorizationCode: code, redirectUri }),

  status: () => axios.get<BankStatusResponse>('/api/bank/status'),

  claims: () => axios.get<{ claims: ComputedClaim[] }>('/api/bank/claims'),
};

export const credentialApi = {
  issue: (req: CredentialIssueRequest) => axios.post<CredentialResponse>('/api/credentials/issue', req),

  list: (page = 0, size = 20) => axios.get<CredentialListResponse>(`/api/credentials?page=${page}&size=${size}`),

  get: (id: string) => axios.get<CredentialResponse>(`/api/credentials/${id}`),

  present: (id: string, req: PresentRequest) => axios.post<PresentationResponse>(`/api/credentials/${id}/present`, req),

  revoke: (id: string) => axios.post<CredentialResponse>(`/api/credentials/${id}/revoke`),
};

export const verifyApi = {
  verify: (token: string) => axios.post<VerifyResponse>('/api/verify', { token }),
};

export const apiKeyApi = {
  create: (label: string) => axios.post<ApiKeyResponse>('/api/verifier-keys', { label }),

  list: () => axios.get<{ keys: ApiKeyResponse[] }>('/api/verifier-keys'),

  revoke: (id: string) => axios.delete(`/api/verifier-keys/${id}`),
};
