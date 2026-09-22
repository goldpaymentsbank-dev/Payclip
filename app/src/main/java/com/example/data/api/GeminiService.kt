package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.VideoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ModerationResult(
    val approved: Boolean,
    val safetyScore: Float,
    val reason: String,
    val suggestedCategory: String
)

object GeminiService {
    private const val TAG = "GeminiService"
    private const val MODEL_NAME = "gemini-2.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private fun getApiKey(): String {
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            if (key.isBlank() || key == "MY_GEMINI_API_KEY") "" else key
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * "Para Ti" AI Recommendation Engine:
     * Takes the user's recent watch history categories and queries Gemini 2.5 Flash
     * to dynamically reorder and rank the video feed.
     */
    suspend fun getPersonalizedFeedOrdering(
        userWatchedCategories: List<String>,
        availableVideos: List<VideoEntity>
    ): List<String> = withContext(Dispatchers.IO) {
        if (availableVideos.isEmpty()) return@withContext emptyList()

        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            Log.d(TAG, "No GEMINI_API_KEY set, using smart local recommendation ordering")
            return@withContext fallbackRecommendation(userWatchedCategories, availableVideos)
        }

        try {
            val videoCatalog = availableVideos.joinToString("; ") { "${it.id}:${it.category}:${it.title}" }
            val historySummary = if (userWatchedCategories.isEmpty()) "General trending content" else userWatchedCategories.joinToString(", ")

            val prompt = """
                You are the AI Recommendation Engine for a TikTok-style video platform feed ("Para Ti").
                User recent watch history interests: [$historySummary].
                Available videos list [ID:Category:Title]:
                $videoCatalog
                
                Task: Reorder and rank these video IDs from most engaging to least engaging for this user.
                Output ONLY a JSON array of the video IDs in order, for example: ["vid_004", "vid_001", "vid_002", "vid_003"]
                Do NOT output markdown code blocks or explanations, just the JSON array.
            """.trimIndent()

            val requestBodyJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
            }

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val responseString = response.body?.string() ?: ""
                val jsonResponse = JSONObject(responseString)
                val candidates = jsonResponse.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val text = firstCandidate?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text", "") ?: ""

                val cleanJson = text.trim()
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                val orderedIds = mutableListOf<String>()
                val jsonArray = JSONArray(cleanJson)
                for (i in 0 until jsonArray.length()) {
                    val id = jsonArray.getString(i)
                    if (availableVideos.any { it.id == id }) {
                        orderedIds.add(id)
                    }
                }

                // Add any missing videos
                for (v in availableVideos) {
                    if (!orderedIds.contains(v.id)) {
                        orderedIds.add(v.id)
                    }
                }

                if (orderedIds.isNotEmpty()) {
                    return@withContext orderedIds
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Gemini recommendation call exception: ${e.message}")
        }

        fallbackRecommendation(userWatchedCategories, availableVideos)
    }

    /**
     * Automated Video Moderation:
     * Sends title and description to Gemini AI to ensure safety (anti-spam / anti-fraud)
     * before publishing into the database.
     */
    suspend fun moderateVideo(
        title: String,
        description: String
    ): ModerationResult = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            return@withContext localModerationCheck(title, description)
        }

        try {
            val prompt = """
                You are an automated video safety and moderation AI for a TikTok-style video platform.
                Analyze the following user-submitted video metadata:
                Title: "$title"
                Description: "$description"
                
                Check for spam, fraud, Ponzi schemes, illegal transactions, hateful content, or extreme violations.
                Return ONLY a JSON object formatted strictly as:
                {
                  "approved": true,
                  "safetyScore": 0.95,
                  "reason": "Content is safe, informative and complies with community guidelines.",
                  "suggestedCategory": "Fintech"
                }
                Do not include markdown or backticks.
            """.trimIndent()

            val requestBodyJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
            }

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val responseString = response.body?.string() ?: ""
                val jsonResponse = JSONObject(responseString)
                val candidates = jsonResponse.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val text = firstCandidate?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text", "") ?: ""

                val cleanJson = text.trim()
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                val resultObj = JSONObject(cleanJson)
                return@withContext ModerationResult(
                    approved = resultObj.optBoolean("approved", true),
                    safetyScore = resultObj.optDouble("safetyScore", 0.95).toFloat(),
                    reason = resultObj.optString("reason", "Aprobado por Gemini 2.5 Flash"),
                    suggestedCategory = resultObj.optString("suggestedCategory", "Trending")
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Gemini video moderation exception: ${e.message}")
        }

        localModerationCheck(title, description)
    }

    private fun fallbackRecommendation(
        userWatchedCategories: List<String>,
        availableVideos: List<VideoEntity>
    ): List<String> {
        val categoryCounts = userWatchedCategories.groupingBy { it }.eachCount()
        return availableVideos.sortedByDescending { video ->
            val matchScore = categoryCounts[video.category] ?: 0
            matchScore * 1000 + video.likesCount
        }.map { it.id }
    }

    private fun localModerationCheck(title: String, description: String): ModerationResult {
        val combined = "$title $description".lowercase()
        val spamWords = listOf("hack dinero", "estafa", "piramide ilegal", "multiplica x100 en 1 hora", "robo de cuentas")
        val isSpam = spamWords.any { combined.contains(it) }

        return if (isSpam) {
            ModerationResult(
                approved = false,
                safetyScore = 0.15f,
                reason = "El contenido contiene términos asociados a fraude o promesas no permitidas.",
                suggestedCategory = "Revisión"
            )
        } else {
            val category = when {
                combined.contains("spei") || combined.contains("dinero") || combined.contains("banco") -> "Fintech"
                combined.contains("juego") || combined.contains("gamer") || combined.contains("setup") -> "Gaming"
                combined.contains("ia") || combined.contains("ai") || combined.contains("tech") -> "Tech"
                else -> "Lifestyle"
            }
            ModerationResult(
                approved = true,
                safetyScore = 0.98f,
                reason = "Aprobado con éxito. Contenido apto para la comunidad y monetización.",
                suggestedCategory = category
            )
        }
    }
}
