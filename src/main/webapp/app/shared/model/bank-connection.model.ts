import dayjs from 'dayjs';

import { BankConnectionStatus } from 'app/shared/model/enumerations/bank-connection-status.model';

export interface IBankConnection {
  id?: number;
  holderLogin?: string;
  connectedAt?: dayjs.Dayjs;
  status?: keyof typeof BankConnectionStatus;
  maskedAccount?: string | null;
  accountType?: string | null;
}

export const defaultValue: Readonly<IBankConnection> = {};
