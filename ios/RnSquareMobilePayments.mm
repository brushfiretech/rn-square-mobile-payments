//
//  RnSquareMobilePayments.m
//  rn-square-mobile-payments
//
//  Created by Matthew Lamperski on 6/10/24.
//

#import "React/RCTBridgeModule.h"
#import <React/RCTEventEmitter.h>
#import <React/RCTUtils.h>

@interface RCT_EXTERN_MODULE(SquareMobilePayments, RCTEventEmitter)

RCT_EXTERN_METHOD(authorizeMobilePaymentsSDK:(NSString *)accessToken
                  locationId:(NSString *)locationId
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(deauthorizeMobilePaymentsSDK:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(startPairing:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(stopPairing:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(startPayment:(NSDictionary *)paymentParams
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(addReaderObserver:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(removeReaderObserver:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(presentSettings:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(getReaders:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(getAuthorizationState:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(getAuthorizationLocation:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(forgetReader:(NSInteger *)readerId
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(cancelPayment:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)
@end
