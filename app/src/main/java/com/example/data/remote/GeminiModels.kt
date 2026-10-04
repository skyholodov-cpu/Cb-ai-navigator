package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @param:Json(name = "contents") val contents: List<ContentItem>,
    @param:Json(name = "systemInstruction") val systemInstruction: ContentItem? = null,
    @param:Json(name = "generationConfig") val generationConfig: GenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class ContentItem(
    @param:Json(name = "role") val role: String? = null,
    @param:Json(name = "parts") val parts: List<PartItem>
)

@JsonClass(generateAdapter = true)
data class PartItem(
    @param:Json(name = "text") val text: String? = null,
    @param:Json(name = "inlineData") val inlineData: InlineDataItem? = null
)

@JsonClass(generateAdapter = true)
data class InlineDataItem(
    @param:Json(name = "mimeType") val mimeType: String,
    @param:Json(name = "data") val data: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @param:Json(name = "temperature") val temperature: Float? = 0.4f,
    @param:Json(name = "topP") val topP: Float? = 0.9f,
    @param:Json(name = "topK") val topK: Int? = 40
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @param:Json(name = "candidates") val candidates: List<CandidateItem>? = null,
    @param:Json(name = "promptFeedback") val promptFeedback: PromptFeedbackItem? = null
)

@JsonClass(generateAdapter = true)
data class CandidateItem(
    @param:Json(name = "content") val content: ContentItem? = null,
    @param:Json(name = "finishReason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class PromptFeedbackItem(
    @param:Json(name = "blockReason") val blockReason: String? = null
)
