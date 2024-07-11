package com.rnsquaremobilepayments

import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.WritableNativeMap
import com.squareup.sdk.mobilepayments.authorization.AuthorizedLocation
import com.squareup.sdk.mobilepayments.cardreader.ReaderInfo
import com.squareup.sdk.mobilepayments.payment.CurrencyCode
import com.squareup.sdk.mobilepayments.payment.DelayAction
import com.squareup.sdk.mobilepayments.payment.Payment
import com.squareup.sdk.mobilepayments.payment.ProcessingMode

object Mappers {
  fun mapFromLocation(location: AuthorizedLocation): WritableMap {
    return WritableNativeMap().apply {
      putString("currency", location.currencyCode.name)
      putString("id", location.locationId)
      putString("mcc", location.merchantId)
      putString("name", location.name)
    }
  }

  fun mapFromReader(readerInfo: ReaderInfo): WritableMap {
    return WritableNativeMap().apply {
      putString("id", readerInfo.id)
      putString("model", mapFromModel(readerInfo.model))
      putString("state", mapFromState(readerInfo.state))
      putString("serialNumber", readerInfo.serialNumber ?: "")
      putString("name", readerInfo.name)
      readerInfo.batteryStatus?.let { putMap("batteryStatus", mapFromBatteryStatus(it)) }
      putBoolean("isForgettable", readerInfo.isForgettable)
      putBoolean("isBlinkable", readerInfo.isBlinkable)
      readerInfo.firmwareVersion?.let { putMap("firmwareInfo", mapFromFirmwareInfo(it, readerInfo.firmwarePercent)) }
    }
  }

  private fun mapFromModel(model: ReaderInfo.Model): String {
    return when (model) {
      ReaderInfo.Model.MAGSTRIPE -> "magstripe"
      ReaderInfo.Model.CONTACTLESS_AND_CHIP -> "contactlessAndChip"
      ReaderInfo.Model.TAP_TO_PAY -> "tapToPay"
    }
  }

  private fun mapFromState(state: ReaderInfo.State): String {
    return when (state) {
      is ReaderInfo.State.Disconnected -> "disconnected"
      is ReaderInfo.State.Connecting -> "connecting"
      is ReaderInfo.State.UpdatingFirmware -> "updatingFirmware"
      is ReaderInfo.State.FailedToConnect -> "failedToConnect"
      is ReaderInfo.State.Disabled -> "disabled"
      is ReaderInfo.State.Ready -> "ready"
      else -> "unknown"
    }
  }

  private fun mapFromBatteryStatus(batteryStatus: ReaderInfo.BatteryStatus): WritableMap {
    return WritableNativeMap().apply {
      putBoolean("isCharging", batteryStatus.isCharging)
      putString("level", mapFromBatteryLevel(batteryStatus.percent))
      putInt("percentage", batteryStatus.percent)
    }
  }

  private fun mapFromBatteryLevel(percent: Int): String {
    return when {
      percent <= 10 -> "criticallyLow"
      percent <= 20 -> "low"
      percent <= 50 -> "mid"
      percent <= 80 -> "high"
      percent <= 100 -> "full"
      else -> "unknown"
    }
  }

  private fun mapFromFirmwareInfo(firmwareVersion: String, firmwarePercent: Int?): WritableMap {
    return WritableNativeMap().apply() {
      putString("version", firmwareVersion)
      firmwarePercent?.let { putInt("updatePercentage", it) }
    }
  }

  fun mapToCurrencyCode(currency: String): CurrencyCode? {
    return when (currency.uppercase()) {
      "AUD" -> CurrencyCode.AUD
      "CAD" -> CurrencyCode.CAD
      "EUR" -> CurrencyCode.EUR
      "GBP" -> CurrencyCode.GBP
      "JPY" -> CurrencyCode.JPY
      "USD" -> CurrencyCode.USD
      else -> null
    }
  }

  fun mapToDelayAction(delayAction: String?): DelayAction {
    if (delayAction == null) return DelayAction.CANCEL
    return when (delayAction.uppercase()) {
      "CANCEL" -> DelayAction.CANCEL
      "COMPLETE" -> DelayAction.COMPLETE
      else -> DelayAction.CANCEL
    }
  }

  fun mapToProcessingMode(mode: String?): ProcessingMode {
    if (mode == null) return ProcessingMode.ONLINE_ONLY
    return when (mode.uppercase()) {
      "AUTO_DETECT" -> ProcessingMode.AUTO_DETECT
      "OFFLINE_ONLY" -> ProcessingMode.OFFLINE_ONLY
      "ONLINE_ONLY" -> ProcessingMode.ONLINE_ONLY
      else -> ProcessingMode.ONLINE_ONLY
    }
  }

  private fun mapFromSourceType(type: Payment.SourceType): WritableMap {
    return WritableNativeMap().apply {
      putString("name", type.name)
      putInt("ordinal", type.ordinal)
    }
  }

  fun mapFromPayment(payment: Payment.OnlinePayment): WritableMap {
    return WritableNativeMap().apply {
      putString("id", payment.id)
      putString("createdAt", payment.createdAt.toISO8601())
      putString("updatedAt", payment.updatedAt.toISO8601())
      putInt("amountMoney", payment.amountMoney.amount.toInt())
      putString("currency", payment.totalMoney.currencyCode.name)
      putInt("tipMoney", payment.tipMoney?.amount?.toInt() ?: 0)
      putInt("appFeeMoney", payment.appFeeMoney?.amount?.toInt() ?: 0)
      putInt("totalMoney", payment.totalMoney.amount.toInt())
      putString("locationId", payment.locationId)
      putString("orderId", payment.orderId)
      putString("referenceId", payment.referenceId)
      putMap("sourceType", mapFromSourceType(payment.sourceType))
    }
  }

}

