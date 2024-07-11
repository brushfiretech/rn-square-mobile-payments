//
//  RnSquareMobilePayments.swift
//  rn-square-mobile-payments
//
//  Created by Matthew Lamperski on 6/10/24.
//

import Foundation
import SquareMobilePaymentsSDK
import React

@objc(SquareMobilePayments)
class SquareMobilePayments: RCTEventEmitter {
  private var pairingHandle: PairingHandle?
  private var startPairingResolver: RCTPromiseResolveBlock?
  private var startPairingRejecter: RCTPromiseRejectBlock?
  private var stopPairingResolver: RCTPromiseResolveBlock?
  private var stopPairingRejecter: RCTPromiseRejectBlock?

  private var paymentHandle: PaymentHandle?
  private var startPaymentResolver: RCTPromiseResolveBlock?
  private var startPaymentRejecter: RCTPromiseRejectBlock?

  @objc
  static override func requiresMainQueueSetup() -> Bool {
    return true
  }

  @objc
  override func supportedEvents() -> [String]! {
    return ["READER_ADDED", "READER_REMOVED", "READER_CHANGED"]
  }

  @objc
  func authorizeMobilePaymentsSDK(_ accessToken: String, locationId: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {

    DispatchQueue.main.async {

      print("Authorizing From Native Layer", accessToken, locationId)

      guard MobilePaymentsSDK.shared.authorizationManager.state == .notAuthorized else {
        // already authorized
        reject("AUTH_ERROR", "ALREADY_AUTHORIZED", nil)
        return
      }

      MobilePaymentsSDK.shared.authorizationManager.authorize(withAccessToken: accessToken, locationID: locationId) { error in
        guard let authError = error else {
          // success
          resolve("Mobile Payments SDK successfully authorized")
          return
        }

        reject("AUTH_ERROR", authError.localizedDescription, nil)
      }

    }

  }

  @objc
  func deauthorizeMobilePaymentsSDK(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    DispatchQueue.main.async {
      MobilePaymentsSDK.shared.authorizationManager.deauthorize {
        resolve("Mobile Payments SDK successfully deauthorized")
      }
    }
  }

  @objc
  func addReaderObserver(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    DispatchQueue.main.async {
      MobilePaymentsSDK.shared.readerManager.add(self)
      resolve("Reader observer successfully added")
    }
  }

  @objc
  func removeReaderObserver(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    DispatchQueue.main.async {
      MobilePaymentsSDK.shared.readerManager.remove(self)
      resolve("Reader observer successfully removed")
    }
  }

  @objc
  func startPairing(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    self.startPairingResolver = resolve
    self.startPairingRejecter = reject
    print("Start pairing form the native layer")
    DispatchQueue.main.async {
      self.pairingHandle = MobilePaymentsSDK.shared.readerManager.startPairing(with: self)
    }
  }

  @objc
  func stopPairing(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    self.stopPairingResolver = resolve
    self.stopPairingRejecter = reject
    print("Stop pairing form the native layer")
    DispatchQueue.main.async {
      if let pairingHandle = self.pairingHandle {
        pairingHandle.stop()
        resolve(nil)
      } else {
        reject("PAIRING_STOP_FAIL", "Something went wrong when trying to stop pairing.", nil)
      }
    }
  }

  private func createPaymentParameters(from paymentParams: [String: Any], reject: @escaping RCTPromiseRejectBlock) -> PaymentParameters? {
    guard let amount = paymentParams["amountMoney"] as? Int,
          let curr = paymentParams["currency"] as? String else {
      reject("INVALID_PAYMENT_PARAMS", "Invalid currency/amount_money", nil)
      return nil;
    }

    let currency = Currency(curr)
    let amountMoney = Money(amount: UInt(amount), currency: currency)
    var params = PaymentParameters(idempotencyKey: UUID().uuidString, amountMoney: amountMoney);

    let optionalParams: [String: (Any) -> Void] = [
      "appFeeMoney": { if let val = $0 as? Int { params.appFeeMoney = Money(amount: UInt(val), currency: currency)} },
      "autoComplete": { if let val = $0 as? Bool { params.autocomplete = val } },
      "locationId": { if let val = $0 as? String { params.locationID = val } },
      "orderId": { if let val = $0 as? String { params.orderID = val } },
      "note": { if let val = $0 as? String { params.note = val } },
      "referenceId": { if let val = $0 as? String { params.referenceID = val } }
    ]

    for (key, setter) in optionalParams {
      if let val = paymentParams[key] {
        setter(val)
      }
    }

    print("Payment Parameters: ", params);
    return params;
  }

  @objc
  func startPayment(_ paymentParams: [String: Any], resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    self.startPaymentResolver = resolve
    self.startPaymentRejecter = reject
    print("Starting payment from the native layer")

    DispatchQueue.main.async {
      guard let viewController = RCTPresentedViewController() else {
        reject("INVALID_VIEW_CONTROLLER", "RCTPresentedViewController did not work...", nil)
        return
      }

      guard let params = self.createPaymentParameters(from: paymentParams, reject: reject) else {
        return;
      }

      self.paymentHandle = MobilePaymentsSDK.shared.paymentManager.startPayment(params, promptParameters: PromptParameters(
          mode: .default,
          additionalMethods: .all
      ), from: viewController, delegate: self)
    }
  }
    
    @objc
    func cancelPayment(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
      guard let handle = self.paymentHandle else {
        reject("PAYMENT_NOT_CANCELLED", "No payment in progress.", nil)
        return
      }
      
      let cancelled = handle.cancelPayment()
      if (cancelled) {
        resolve("Payment cancelled.")
      } else {
        reject("PAYMENT_NOT_CANCELLED", "Could not cancel payment.", nil)
      }
    }

  @objc
  func presentSettings(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    DispatchQueue.main.async {
      if let rootViewController = RCTPresentedViewController() {
        MobilePaymentsSDK.shared.settingsManager.presentSettings(with: rootViewController) { error in
          if let error = error {
            reject("SETTINGS_ERROR", error.localizedDescription, nil)
          } else {
            resolve("Settings presented successfully")
          }
        }
      }
    }
  }

  @objc
  func getReaders(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    let readers: [ReaderInfo] = MobilePaymentsSDK.shared.readerManager.readers
    let mappedReaders = readers.map { reader in
        return Mappers.mapFromReader(reader)
    }
    resolve(mappedReaders)
  }

  @objc
  func getAuthorizationState(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
      let state = MobilePaymentsSDK.shared.authorizationManager.state
      let stateString = Mappers.mapFromAuthorizationState(state)
      resolve(stateString)
  }

  @objc
  func getAuthorizationLocation(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
      var locationDict: NSDictionary?
      if let location = MobilePaymentsSDK.shared.authorizationManager.location {
          locationDict = Mappers.mapFromLocation(location)
      }
      resolve(locationDict as Any)
  }
  
  @objc
  func forgetReader(_ readerId: Int, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    let readers: [ReaderInfo] = MobilePaymentsSDK.shared.readerManager.readers
    readers.forEach { reader in
      if reader.id == Int(readerId) ?? 0 {
          if (reader.isForgettable) {
            MobilePaymentsSDK.shared.readerManager.forget(reader)
            resolve("Forgot reader \(reader.serialNumber ?? "")")
          } else {
            reject("Cannot forget reader", "Reader not forgettable", nil)
          }
        }
    }
    
  }

}

extension SquareMobilePayments: ReaderPairingDelegate {
    func readerPairingDidBegin() {
       print("Reader pairing did begin!")
    }

    func readerPairingDidSucceed() {
      print("Reader Paired!")
      startPairingResolver?("Reader Paired!")
      startPairingResolver = nil
      startPairingRejecter = nil
    }

    func readerPairingDidFail(with error: Error) {
        // Handle pairing error
      print("Error: \(error.localizedDescription)")
      startPairingRejecter?("PAIRING_FAILED", error.localizedDescription, error)
      startPairingResolver = nil
      startPairingRejecter = nil
    }
}

extension SquareMobilePayments: PaymentManagerDelegate {
  func paymentManager(_ paymentManager: PaymentManager, didFinish payment: Payment) {
    print("Payment complete!!", payment)
    startPaymentResolver?(Mappers.mapFromPayment(payment))
    startPairingRejecter = nil
    startPairingRejecter = nil
  }

  func paymentManager(_ paymentManager: PaymentManager, didFail payment: Payment, withError error: Error) {
    print("Payment Failed!", error.localizedDescription);
    startPaymentRejecter?("PAYMENT_FAILED", error.localizedDescription, nil);
    startPairingResolver = nil
    startPaymentRejecter = nil
  }

  func paymentManager(_ paymentManager: PaymentManager, didCancel payment: Payment) {
    print("Payment Cancelled", payment)
    startPaymentRejecter?("PAYMENT_CANCELLED", "PAYMENT_CANCELLED", nil);
    startPaymentResolver = nil
    startPaymentRejecter = nil
  }


}

extension SquareMobilePayments: ReaderObserver {
  func readerWasAdded(_ readerInfo: ReaderInfo) {
      print("[Square Reader] reader was added!", readerInfo)
    sendEvent(withName: "READER_ADDED", body: ["readerInfo": Mappers.mapFromReader(readerInfo)])
  }

  func readerWasRemoved(_ readerInfo: ReaderInfo) {
      print("[Square Reader] reader was removed!", readerInfo)
      sendEvent(withName: "READER_REMOVED", body: ["readerInfo": Mappers.mapFromReader(readerInfo)])
  }

  func readerDidChange(_ readerInfo: ReaderInfo, change: ReaderChange) {
      print("[Square Reader] reader was changed!", readerInfo)
    sendEvent(withName: "READER_CHANGED", body: ["readerInfo": Mappers.mapFromReader(readerInfo), "change": Mappers.mapFromChange(change)])
  }
}
