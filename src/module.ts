import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'rn-square-mobile-payments' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const SquareMobilePayments = NativeModules.SquareMobilePayments
  ? NativeModules.SquareMobilePayments
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export default SquareMobilePayments;
