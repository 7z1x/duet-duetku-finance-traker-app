package com.duetduetku.app.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyFormatter {
    
    fun getSymbol(): String {
        return "Rp"
    }

    fun format(amount: Double): String {
        return try {
            val targetLocale = Locale("id", "ID")
            val specificFormat = NumberFormat.getCurrencyInstance(targetLocale)
            specificFormat.maximumFractionDigits = 0
            specificFormat.format(amount)
        } catch (e: Exception) {
            "Rp $amount"
        }
    }

    // Helper for Manual Input where we type 10000 -> "10.000" (IDR)
    fun formatNumber(amount: Double): String {
         val targetLocale = Locale("id", "ID")
         val format = NumberFormat.getNumberInstance(targetLocale)
         return format.format(amount)
    }
}
