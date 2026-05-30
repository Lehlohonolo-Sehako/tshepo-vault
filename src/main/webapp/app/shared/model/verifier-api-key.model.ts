import dayjs from 'dayjs';

export interface IVerifierApiKey {
  id?: number;
  ownerLogin?: string;
  label?: string;
  keyHash?: string;
  active?: boolean;
  callCount?: number | null;
  createdAt?: dayjs.Dayjs;
}

export const defaultValue: Readonly<IVerifierApiKey> = {
  active: false,
};
