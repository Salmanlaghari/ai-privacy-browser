package com.aibrowser.app.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

// Gemini generateContent request data structures
data class GeminiRequest(val contents: List<Content>)
data class Content(val parts: List<Part>)
data class Part(val text: String)

// Gemini generateContent response data structures
data class GeminiResponse(val candidates: List<Candidate>?)
data class Candidate(val content: ContentResponse?)
data class ContentResponse(val parts: List<PartResponse>?)
data class PartResponse(val text: String?)

/**
 * Retrofit interface for Google Gemini API generateContent endpoint.
 */
interface AiService {

    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
}
