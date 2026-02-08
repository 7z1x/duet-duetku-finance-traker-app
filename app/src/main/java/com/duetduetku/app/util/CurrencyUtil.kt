package com.duetduetku.app.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtil {
    fun formatRp(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        format.maximumFractionDigits = 0
        val currency = format.format(amount)
        return currency.replace("Rp", "Rp ").trim()
    }
}
