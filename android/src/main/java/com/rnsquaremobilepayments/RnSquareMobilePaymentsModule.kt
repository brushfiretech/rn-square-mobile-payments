package com.rnsquaremobilepayments

import android.app.Application
import android.os.Handler
import android.os.Looper
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.WritableNativeArray
import com.facebook.react.bridge.WritableNativeMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.squareup.sdk.mobilepayments.MobilePaymentsSdk
import com.squareup.sdk.mobilepayments.cardreader.PairingHandle
import com.squareup.sdk.mobilepayments.cardreader.ReaderChangedEvent.Change
import com.squareup.sdk.mobilepayments.core.CallbackReference
import com.squareup.sdk.mobilepayments.core.Result
import com.squareup.sdk.mobilepayments.payment.AdditionalPaymentMethod
import com.squareup.sdk.mobilepayments.payment.Money
import com.squareup.sdk.mobilepayments.payment.Payment
import com.squareup.sdk.mobilepayments.payment.PaymentHandle
import com.squareup.sdk.mobilepayments.payment.PaymentParameters
import com.squareup.sdk.mobilepayments.payment.PromptMode
import com.squareup.sdk.mobilepayments.payment.PromptParameters
import java.util.UUID

class RnSquareMobilePaymentsModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private var callbackReference: CallbackReference? = null
  private var pairingHandle: PairingHandle? = null
  private var paymentHandle: PaymentHandle? = null
  private val reactAppContext = reactContext
  override fun getName() = "SquareMobilePayments"

  private fun sendEvent(reactContext: ReactContext, eventName: String, params: WritableMap) {
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit(eventName, params)
  }

  @ReactMethod
  fun getAuthorizationState(promise: Promise) {
    val authManager = MobilePaymentsSdk.authorizationManager()
    val stateString = when {
      authManager.authorizationState.isAuthorizationInProgress -> "authorizing"
      authManager.authorizationState.isAuthorized -> "authorized"
      else -> "notAuthorized"
    }
    promise.resolve(stateString)
  }

  @ReactMethod
  fun deauthorizeMobilePaymentsSDK(promise: Promise) {
    val authManager = MobilePaymentsSdk.authorizationManager()
    authManager.deauthorize()
    promise.resolve("Mobile Payments SDK successfully deauthorized")
  }

  @ReactMethod
  fun authorizeMobilePaymentsSDK(accessToken: String, locationId: String, promise: Promise) {
    val authManager = MobilePaymentsSdk.authorizationManager()
    if (!authManager.authorizationState.isAuthorized) {
      authManager.authorize(accessToken, locationId) { result ->
        when (result) {
          is Result.Success -> {
            promise.resolve("Mobile Payments SDK successfully authorized")
          }
          is Result.Failure -> {
            promise.reject("AUTH_ERROR", result.errorMessage)
          }
          else -> {
            promise.reject("AUTH_ERROR", "UNKNOWN_ERROR")
          }
        }
      }
    } else {
      promise.reject("AUTH_ERROR", "ALREADY_AUTHORIZED")
    }
  }

  @ReactMethod
  fun getAuthorizationLocation(promise: Promise) {
    val authManager = MobilePaymentsSdk.authorizationManager()
    val location = authManager.authorizedLocation

    if (location == null) {
      promise.reject("LOCATION_ERROR", "SDK must be authorized to get location")
    } else {
      val locationDict = location?.let { Mappers.mapFromLocation(it) }
      promise.resolve(locationDict as Any)
    }
  }

  @ReactMethod
  fun presentSettings(promise: Promise) {
    val handler = Handler(Looper.getMainLooper())
    handler.post {
      val currentActivity = currentActivity
      if (currentActivity != null) {
        MobilePaymentsSdk.settingsManager().showSettings { result ->
          when (result) {
            is Result.Success -> {
              promise.resolve("Settings presented successfully")
            }
            is Result.Failure -> {
              promise.reject("SETTINGS_ERROR", result.errorMessage ?: "Unknown error")
            }
          }
        }
      } else {
        promise.reject("ACTIVITY_ERROR", "Current activity is null")
      }
    }
  }

  @ReactMethod
  fun addReaderObserver(promise: Promise) {
    val readerManager = MobilePaymentsSdk.readerManager()
    callbackReference = readerManager.setReaderChangedCallback { event ->
      val reader = event.reader
      val change = event.change
      val mappedReader = Mappers.mapFromReader(reader)
      val readerEvent = WritableNativeMap().apply {
        putMap("readerInfo", mappedReader.copy())
      }
      when (change) {
        Change.ADDED -> {
          sendEvent(reactAppContext, "READER_ADDED", readerEvent )
        }
        Change.REMOVED -> {
          sendEvent(reactAppContext, "READER_REMOVED", readerEvent )
        }
        Change.CHANGED_STATE -> {
          val eventMap = WritableNativeMap().apply {
            putMap("readerInfo", mappedReader)
            putString("change", "stateDidChange")
          }
          sendEvent(reactAppContext, "READER_CHANGED", eventMap)
        }
        Change.BATTERY_THRESHOLD -> {
          val eventMap = WritableNativeMap().apply {
            putMap("readerInfo", mappedReader)
            putString("change", "batteryLevelDidChange")
          }
          sendEvent(reactAppContext, "READER_CHANGED", eventMap)
        }
        Change.BATTERY_CHARGING -> {
          val eventMap = WritableNativeMap().apply {
            putMap("readerInfo", mappedReader)
            putString("change", "batteryDidBeginCharging")
          }
          sendEvent(reactAppContext, "READER_CHANGED", eventMap)
        }
        Change.FIRMWARE_PROGRESS -> {
          val eventMap = WritableNativeMap().apply {
            putMap("readerInfo", mappedReader)
            putString("change", "firmwareProgress")
          }
          sendEvent(reactAppContext, "READER_CHANGED", eventMap)
        }
      }
    }
    promise.resolve("Reader observer successfully added")
  }

  @ReactMethod
  fun removeReaderObserver(promise: Promise) {
    callbackReference?.clear()
    promise.resolve("Reader observer successfully removed")
  }

  @ReactMethod
  fun startPairing(promise: Promise) {
    val readerManager = MobilePaymentsSdk.readerManager()

    if (!readerManager.isPairingInProgress) {
      pairingHandle = readerManager.pairReader { result ->
        when (result) {
          is Result.Success -> {
            val readerPaired = result.value
            if (readerPaired) {
              // The info will be reported in the reader changed event
              promise.resolve("Reader paired!")
            } else {
              promise.reject("PAIRING_CANCELLED", "Reader pairing cancelled.")
            }
          }
          is Result.Failure -> {
            promise.reject("PAIRING_FAILED", result.errorMessage)
          }
        }
      }
    } else {
      promise.reject("PAIRING_IN_PROGRESS", "Pairing already in progress.")
    }
  }

  @ReactMethod
  fun stopPairing(promise: Promise) {
    var stopped = false
    pairingHandle?.let {
      it.stop()
      stopped = true
    }
    if (stopped) {
      promise.resolve(null)
    } else {
      promise.reject("PAIRING_STOP_FAIL", "Something went wrong when trying to stop pairing.")
    }
  }

  private fun createPaymentParameters(paymentParams: ReadableMap, promise: Promise): PaymentParameters? {
    val currencyCode = paymentParams.getString("currency")?.let {
      Mappers.mapToCurrencyCode(it)
    }
    val money = currencyCode?.let {
      Money(paymentParams.getInt("amountMoney").toLong(), it)
    }
    if (money === null || money.amount == 0L) {
      promise.reject("INVALID_PAYMENT_PARAMS", "Invalid currency/amountMoney")
      return null
    }
    val params = PaymentParameters.Builder(
      amount = money,
      idempotencyKey = UUID.randomUUID().toString()
    )
      .acceptPartialAuthorization(paymentParams.getBooleanOr("acceptPartialAuthorization", false))
      .appFeeMoney(if (paymentParams.hasKey("appFeeMoney")) Money(paymentParams.getInt("appFeeMoney").toLong(), currencyCode) else null)
      .autocomplete(paymentParams.getBooleanOr("autoComplete", true))
      .customerId(paymentParams.getString("customerId"))
      .delayAction(Mappers.mapToDelayAction(paymentParams.getString("delayAction")))
      .delayDuration(if (paymentParams.hasKey("delayDuration") && paymentParams.getInt("delayDuration") != 0) paymentParams.getInt("delayDuration").toLong() else null)
      .locationId(paymentParams.getString("locationId"))
      .note(paymentParams.getString("note"))
      .orderId(paymentParams.getString("orderId"))
      .processingMode(Mappers.mapToProcessingMode("processingMode"))
      .referenceId(paymentParams.getString("referenceId"))
      .statementDescription(paymentParams.getString("statementDescription"))
      .teamMemberId(paymentParams.getString("teamMemberId"))
      .tipMoney(if (paymentParams.hasKey("tipMoney")) Money(paymentParams.getInt("tipMoney").toLong(), currencyCode) else null)
      .build()
    return params
  }

  @ReactMethod
  fun startPayment(paymentParams: ReadableMap, promise: Promise) {
    val paymentManager = MobilePaymentsSdk.paymentManager()
    val paymentParameters = createPaymentParameters(paymentParams, promise) ?: return // if this is null, it will already be rejected by this f(x)
    val promptParameters = PromptParameters(
      mode = PromptMode.DEFAULT,
      additionalPaymentMethods = listOf(AdditionalPaymentMethod.Type.KEYED)
    )

    val handler = Handler(Looper.getMainLooper())
    handler.post {
      paymentHandle =
        paymentManager.startPaymentActivity(paymentParameters, promptParameters) { result ->
          when (result) {

            is Result.Success -> {
              if (result.value is Payment.OnlinePayment) {
                promise.resolve(Mappers.mapFromPayment(result.value as Payment.OnlinePayment))
              } else {
                promise.reject("OFFLINE_PAYMENTS_NOT_SUPPORTED", "offline payments are not supported in this native module")
              }
            }
            is Result.Failure -> promise.reject("PAYMENT_FAILED", result.errorMessage)
          }
        }
    }
  }

  @ReactMethod
  fun forgetReader(readerId: String, promise: Promise) {
    val readers = MobilePaymentsSdk.readerManager().getReaders()
    readers.forEach { reader ->
      if (reader.id == readerId) {
        if (reader.isForgettable) {
          MobilePaymentsSdk.readerManager().forget(reader)
          promise.resolve("Forgot reader ${reader.serialNumber ?: ""}")
        } else {
          promise.reject("Cannot forget reader", "Reader not forgettable")
        }
      }
    }
  }

  @ReactMethod
  fun cancelPayment(promise: Promise) {
    paymentHandle?.let {
      val result = it.cancel()
      if (result == PaymentHandle.CancelResult.CANCELED)
        promise.resolve("Payment cancelled.")
      else {
        if (result == PaymentHandle.CancelResult.NO_PAYMENT_IN_PROGRESS) {
          promise.reject("PAYMENT_NOT_CANCELLED", "No payment in progress.")
        } else {
          promise.reject("PAYMENT_NOT_CANCELLED", "Payment could not be cancelled.")
        }
      }
      return
    }
    promise.reject("PAYMENT_NOT_CANCELLED", "No payment in progress.")
  }

  @ReactMethod
  fun getReaders(promise: Promise) {
    val readers = MobilePaymentsSdk.readerManager().getReaders()
    val mappedReaders = WritableNativeArray()
    readers.forEach { reader ->
      mappedReaders.pushMap(Mappers.mapFromReader(reader))
    }
    promise.resolve(mappedReaders)
  }

  companion object {
    @JvmStatic
    fun initializeMobilePaymentsSdk(applicationId: String, application: Application) {
      MobilePaymentsSdk.initialize(applicationId, application)
    }
  }
}
