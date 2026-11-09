package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    private val decimalSymbols = DecimalFormatSymbols(Locale("pt", "MZ")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }

    private val currencyFormat = DecimalFormat("#,##0.00", decimalSymbols)
    private val integerFormat = DecimalFormat("#,##0", decimalSymbols)

    fun formatMoney(amount: Double, currency: String = "MT"): String {
        return "${currencyFormat.format(amount)} $currency"
    }

    fun formatCompactMoney(amount: Double, currency: String = "MT"): String {
        return if (amount % 1.0 == 0.0) {
            "${integerFormat.format(amount)} $currency"
        } else {
            "${currencyFormat.format(amount)} $currency"
        }
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "MZ"))
        return sdf.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "MZ"))
        return sdf.format(Date(timestamp))
    }

    fun formatRelativeDate(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diffDays = ((timestamp - now) / (1000 * 60 * 60 * 24)).toInt()
        return when {
            diffDays < 0 -> "Venceu há ${-diffDays} dias"
            diffDays == 0 -> "Vence hoje"
            diffDays == 1 -> "Vence amanhã"
            else -> "Vence em $diffDays dias"
        }
    }
}
