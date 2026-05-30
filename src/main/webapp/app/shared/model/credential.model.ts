import dayjs from 'dayjs';

import { CredentialStatus } from 'app/shared/model/enumerations/credential-status.model';

export interface ICredential {
  id?: number;
  holderLogin?: string;
  title?: string;
  purpose?: string | null;
  status?: keyof typeof CredentialStatus;
  issuedAt?: dayjs.Dayjs;
  expiresAt?: dayjs.Dayjs;
  issuerDid?: string;
  sdJwt?: string;
  claimsSummary?: string | null;
  vcRef?: string | null;
}

export const defaultValue: Readonly<ICredential> = {};
