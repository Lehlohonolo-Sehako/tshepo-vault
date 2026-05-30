import dayjs from 'dayjs';

import { VerificationResult } from 'app/shared/model/enumerations/verification-result.model';
import { IVerifierApiKey } from 'app/shared/model/verifier-api-key.model';

export interface IVerificationEvent {
  id?: number;
  verifiedAt?: dayjs.Dayjs;
  result?: keyof typeof VerificationResult;
  disclosedClaims?: string | null;
  credentialRef?: string | null;
  apiKey?: IVerifierApiKey | null;
}

export const defaultValue: Readonly<IVerificationEvent> = {};
