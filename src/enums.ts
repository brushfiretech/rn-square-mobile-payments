export enum ReaderModel {
  ContactlessAndChip = 'contactlessAndChip',
  Embedded = 'embedded',
  Magstripe = 'magstripe',
  Stand = 'stand',
  Unknown = 'unknown',
}

export enum ReaderState {
  Connecting = 'connecting',
  Disabled = 'disabled',
  Disconnected = 'disconnected',
  FailedToConnect = 'failedToConnect',
  Ready = 'ready',
  UpdatingFirmware = 'updatingFirmware',
  Unknown = 'unknown',
}

export enum BatteryLevel {
  CriticallyLow = 'criticallyLow',
  Full = 'full',
  High = 'high',
  Low = 'low',
  Mid = 'mid',
  Unknown = 'unknown',
}

export enum AuthorizationState {
  Authorized = 'authorized',
  Authorizing = 'authorizing',
  NotAuthorized = 'notAuthorized',
  Unknown = 'unknown',
}

export enum ReaderChange {
  BatteryDidBeginCharging = 'batteryDidBeginCharging',
  BatteryDidEndCharging = 'batteryDidEndCharging',
  BatteryLevelDidChange = 'batteryLevelDidChange',
  CardInserted = 'cardInserted',
  CardRemoved = 'cardRemoved',
  ConnectionDidFail = 'connectionDidFail',
  ConnectionStateDidChange = 'connectionStateDidChange',
  FirmwareUpdateDidFail = 'firmwareUpdateDidFail',
  FirmwareUpdatePercentDidChange = 'firmwareUpdatePercentDidChange',
  StateDidChange = 'stateDidChange',
  UnknownStateChange = 'unknown state change',
}

export enum ReaderEventType {
  READER_REMOVED = 'READER_REMOVED',
  READER_ADDED = 'READER_ADDED',
  READER_CHANGED = 'READER_CHANGED',
}
