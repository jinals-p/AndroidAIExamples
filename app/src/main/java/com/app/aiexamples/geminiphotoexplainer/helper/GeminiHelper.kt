package com.app.aiexamples.geminiphotoexplainer.helper

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.app.aiexamples.BuildConfig

class GeminiHelper {

    //apiKey = Your Gemini api key
    private val model = GenerativeModel(modelName = "gemini-2.5-flash", apiKey = BuildConfig.GEMINI_API_KEY)

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