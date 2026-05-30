import { ICredential } from 'app/shared/model/credential.model';
import { ClaimOperator } from 'app/shared/model/enumerations/claim-operator.model';
import { ClaimType } from 'app/shared/model/enumerations/claim-type.model';

export interface IIssuedClaim {
  id?: number;
  claimType?: keyof typeof ClaimType;
  operator?: keyof typeof ClaimOperator;
  threshold?: number;
  currency?: string | null;
  periodMonths?: number | null;
  met?: boolean;
  credential?: ICredential;
}

export const defaultValue: Readonly<IIssuedClaim> = {
  met: false,
};
