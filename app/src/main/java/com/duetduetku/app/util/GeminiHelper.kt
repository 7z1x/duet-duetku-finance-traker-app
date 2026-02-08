package com.duetduetku.app.util

import android.graphics.Bitmap
import com.duetduetku.app.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

object GeminiHelper {
    
    private val model = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    data class ReceiptResult(
        val merchant: String? = null,
        val total: Double? = null,
        val date: Long? = null
    )

    suspend fun analyzeReceipt(bitmap: Bitmap): ReceiptResult = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Analyze this receipt image. Extract the following information in JSON format only:
                {
                    "merchant": "Name of the store/merchant",
                    "total": 12345.00 (Numeric double, no currency symbols),
                    "date": "DD/MM/YYYY" (Format)
                }
                If value is not found, use null.
                Return ONLY raw JSON, no markdown formatting like ```json ... ```.
            """.trimIndent()

            val response = model.generateContent(
                content {
                    image(bitmap)
                    text(prompt)
                }
            )

            val responseText = response.text ?: return@withContext ReceiptResult()
            parseJson(responseText)
        } catch (e: Exception) {
            e.printStackTrace()
            ReceiptResult()
        }
    }

    suspend fun analyzeText(text: String): ReceiptResult = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Extract transaction details from this text: "$text".
                
                RULES:
                1. Merchant: Identify the MAIN activity, item, or description of the transaction.
                   - Prioritize the 'WHAT' (Item/Activity) over the 'WHERE' (Merchant/Location).
                   - Example: "Makan rendang di istana negara" -> "Makan rendang".
                   - Example: "Belanja di Indomaret" -> "Belanja di Indomaret" (or just "Belanja").
                   - If only merchant is mentioned (e.g. "Mcdonalds"), use it.
                2. Total: 
                   - Sum up all amounts mentioned (e.g., "Food 15k and Drink 5k" -> 20000).
                   - Handle "ribu", "rb", "k" as thousands (e.g., "15 ribu" = 15000).
                   - If context suggests generic IDR prices (e.g., "15" and "4 ribu"), interpret "15" as 15000.
                   - Return a Numeric Double.
                3. Date: "DD/MM/YYYY" format if mentioned, otherwise null.
                
                Return JSON format only:
                {
                    "merchant": "...",
                    "total": 0.0,
                    "date": "..."
                }
                Return ONLY raw JSON, no markdown.
            """.trimIndent()

            val response = model.generateContent(prompt)

            val responseText = response.text ?: return@withContext ReceiptResult()
            parseJson(responseText)
        } catch (e: Exception) {
            e.printStackTrace()
            ReceiptResult()
        }
    }

    private fun parseJson(jsonStr: String): ReceiptResult {
         try {
             // Clean up potential markdown code blocks if AI ignores instruction
             val cleanJson = jsonStr.replace("```json", "").replace("```", "").trim()
             val json = JSONObject(cleanJson)
             
             val merchant = if (json.has("merchant")) json.getString("merchant") else null
             val total = if (json.has("total")) json.getDouble("total") else null
             val dateStr = if (json.has("date")) json.getString("date") else null
             
             var dateLong: Long? = null
             if (dateStr != null) {
                 dateLong = DateUtil.parseDateString(dateStr)
             }
             
             return ReceiptResult(merchant, total, dateLong)
         } catch (e: Exception) {
             return ReceiptResult()
         }
    }
}
