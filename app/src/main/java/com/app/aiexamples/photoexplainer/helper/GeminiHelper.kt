package com.app.aiexamples.photoexplainer.helper

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content

class GeminiHelper {

    private val model = GenerativeModel(modelName = "gemini-2.5-flash", apiKey = "YOUR_GEMENI_API_KEY")

    /*suspend fun analyzeText(prompt: String): String {
        return try {
            val response = model.generateContent(content {
                text(prompt)
            })
            response.text ?: "No response"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
*/
    suspend fun analyzeImage(bitmap: Bitmap, prompt: String): String {
        return try {
            val response = model.generateContent(content {
                image(bitmap)
                text(prompt)
            })
            response.text ?: "No response"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}