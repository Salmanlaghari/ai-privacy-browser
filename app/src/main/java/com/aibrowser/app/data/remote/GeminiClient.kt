package com.aibrowser.app.data.remote

import com.aibrowser.app.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.io.IOException

/**
 * Handle API communications with Google's Gemini SDK models using Retrofit direct calls.
 */
class GeminiClient {

    private val service: AiService

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        service = retrofit.create(AiService::class.java)
    }

    /**
     * Executes content generation against the gemini-1.5-flash model.
     * Throws custom descriptive exceptions to handle rate limits, invalid keys, or network failures.
     */
    @Throws(Exception::class)
    suspend fun generateContent(prompt: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "placeholder" || apiKey.startsWith("AIzaSyPlaceholder")) {
            throw IllegalArgumentException("AI features require a valid API key. Please configure GEMINI_API_KEY in local.properties or set it as a GitHub Secret.")
        }

        val request = GeminiRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = prompt)
                    )
                )
            )
        )

        val response = try {
            service.generateContent(apiKey, request)
        } catch (e: IOException) {
            Timber.e(e, "Internet connection error during Gemini API call.")
            throw IOException("AI features need internet. Please check your connection.")
        }

        if (response.isSuccessful) {
            val body = response.body()
            val text = body?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (text != null) {
                return text
            } else {
                throw Exception("AI temporarily unavailable. Empty response received.")
            }
        } else {
            val code = response.code()
            val errorBody = response.errorBody()?.string() ?: ""
            Timber.e("Gemini API request failed. Code: %d, Response: %s", code, errorBody)

            when (code) {
                429 -> throw Exception("Too many requests. Rate limit hit. Try again in a minute.")
                400, 403 -> throw Exception("Invalid API key configuration. Please verify your credentials.")
                else -> throw Exception("AI temporarily unavailable. Code $code. Please try again.")
            }
        }
    }
}
