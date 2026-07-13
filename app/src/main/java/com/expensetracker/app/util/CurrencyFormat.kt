package com.expensetracker.app.util

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Pinned to Malaysian Ringgit rather than following the device's locale.
 * NumberFormat.getCurrencyInstance() alone would show whatever currency
 * matches the phone/emulator's region setting (EUR, USD, etc.) — this
 * app is scoped to MYR regardless of where it's run.
 */
//fun myrCurrencyFormat(): NumberFormat =
//    NumberFormat.getCurrencyInstance(Locale("ms", "MY")).apply {
//        currency = Currency.getInstance("MYR")
//
//    }
fun myrCurrencyFormat(): NumberFormat {
    val format = NumberFormat.getCurrencyInstance(Locale("ms", "MY")) as DecimalFormat
    format.currency = Currency.getInstance("MYR")
    format.positivePrefix = "RM "
    format.negativePrefix = "-RM "
    return format
}