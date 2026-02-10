package com.katchy.focuslive.data.remote

data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null
)

data class Content(
    val parts: List<Part>,
    val role: String = "user"
)

data class Part(
    val text: String
)

data class GenerationConfig(
    val temperature: Float = 0.7f,
    val maxOutputTokens: Int = 1000
)

data class GenerateContentResponse(
    val candidates: List<Candidate>? = null,
    val promptFeedback: PromptFeedback? = null,
    val error: GeminiError? = null
)

data class Candidate(
    val content: Content?,
    val finishReason: String?,
    val index: Int?
)

data class PromptFeedback(
    val blockReason: String?
)

data class GeminiError(
    val code: Int?,
    val message: String?,
    val status: String?
)
