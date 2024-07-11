import {
  ReaderChange,
  ReaderEventType,
  ReaderModel,
  ReaderState,
} from './enums';
import type { BatteryLevel } from './enums';

export type SquarePaymentParams = {
  autoComplete: boolean;
  amountMoney: number;
  currency: number;
  appFeeMoney: number;
  locationId: string;
  orderId: string;
  note: string;
  referenceId: string;
};

export type SquarePayment = {
  id: string;
  updatedAt: Date | null;
  referenceId: string | null;
  totalMoney: number;
  sourceType: string;
  tipMoney: number;
  appFeeMoney: number;
  orderId: string;
  locationId: string;
  createdAt: Date | null;
  amountMoney: number;
  currency: string;
};

export interface ReaderInfo {
  id: string;
  model: ReaderModel;
  state: ReaderState;
  serialNumber?: string;
  name: string;
  batteryStatus?: BatteryStatus | null;
  isForgettable: boolean;
  isBlinkable: boolean;
  firmwareInfo?: FirmwareInfo | null;
}

export interface SquareError extends Error {
  code: string;
  message: string;
  nativeError?: any;
}

export interface ReaderLocation {
  currency: string;
  id: string;
  mcc: string;
  name: string;
}

export interface BatteryStatus {
  isCharging: boolean;
  level: BatteryLevel;
  percentage: number;
}

export interface FirmwareInfo {
  failureReason?: any;
  updatePercentage: number;
  version: string;
}

export interface ReaderEventBase {
  readerInfo: ReaderInfo;
}

export interface AddReaderEvent extends ReaderEventBase {}

export interface RemoveReaderEvent extends ReaderEventBase {}

export interface ChangedReaderEvent extends ReaderEventBase {
  change: ReaderChange;
}

export type ISquareEventListener = {
  (
    event: ReaderEventType.READER_ADDED,
    callback: (event: AddReaderEvent) => void
  ): void;
  (
    event: ReaderEventType.READER_CHANGED,
    callback: (event: ChangedReaderEvent) => void
  ): void;
  (
    event: ReaderEventType.READER_REMOVED,
    callback: (event: RemoveReaderEvent) => void
  ): void;
};
