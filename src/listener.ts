import { NativeEventEmitter } from 'react-native';
import { useEffect, useRef } from 'react';
import { addReaderObserver, removeReaderObserver } from './functions';
import type { ReaderEventType } from './enums';
import type { ISquareEventListener } from './types';
import SquareMobilePayments from './module';

export const eventEmitter = new NativeEventEmitter(SquareMobilePayments);

const useSquareEventListener: ISquareEventListener = (
  event: ReaderEventType,
  callback: (...args: any[]) => void
) => {
  const callbackRef = useRef(callback);
  useEffect(() => {
    callbackRef.current = callback;
  }, [callback]);

  useEffect(() => {
    addReaderObserver();

    const subscription = eventEmitter.addListener(event, (...args) => {
      if (callbackRef.current) {
        callbackRef.current(...args);
      }
    });

    return () => {
      subscription.remove();
      removeReaderObserver();
    };
  }, [event]);
};

export { useSquareEventListener };
