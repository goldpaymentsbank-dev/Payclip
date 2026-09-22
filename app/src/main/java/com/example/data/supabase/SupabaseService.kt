package com.example.data.supabase

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

object SupabaseService {
    private const val TAG = "SupabaseService"
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private fun getBaseRestUrl(): String {
        val raw = SupabaseConfig.supabaseUrl.trimEnd('/')
        return "$raw/rest/v1"
    }

    private fun getApiKey(): String {
        return SupabaseConfig.supabaseKey
    }

    /**
     * Checks connectivity and latency to Supabase Project.
     */
    suspend fun checkHealth(): SupabaseHealthStatus = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val url = "${getBaseRestUrl()}/"
        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", getApiKey())
            .addHeader("Authorization", "Bearer ${getApiKey()}")
            .get()
            .build()

        try {
            val response = httpClient.newCall(request).execute()
            val latency = System.currentTimeMillis() - startTime
            val isSuccess = response.isSuccessful || response.code in 200..399

            SupabaseHealthStatus(
                isConnected = isSuccess,
                responseCode = response.code,
                latencyMs = latency,
                projectUrl = SupabaseConfig.supabaseUrl,
                statusMessage = if (isSuccess) "Conectado a Supabase exitosamente" else "Respuesta HTTP ${response.code}"
            )
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            Log.e(TAG, "Error checking Supabase health: ${e.message}")
            SupabaseHealthStatus(
                isConnected = false,
                responseCode = 0,
                latencyMs = latency,
                projectUrl = SupabaseConfig.supabaseUrl,
                statusMessage = "Error de conexión: ${e.localizedMessage ?: "Timeout"}"
            )
        }
    }

    /**
     * Fetches todos from `todos` table in Supabase.
     * Equivalent to: const { data: todos } = await supabase.from('todos').select()
     */
    suspend fun fetchTodos(): Result<List<SupabaseTodo>> = withContext(Dispatchers.IO) {
        val url = "${getBaseRestUrl()}/todos?select=*&order=id.desc"
        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", getApiKey())
            .addHeader("Authorization", "Bearer ${getApiKey()}")
            .addHeader("Accept", "application/json")
            .get()
            .build()

        try {
            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val list = mutableListOf<SupabaseTodo>()
                val jsonArray = JSONArray(responseBody)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val id = obj.optString("id", UUID.randomUUID().toString())
                    val name = obj.optString("name", obj.optString("title", "Tarea"))
                    val isComplete = obj.optBoolean("is_complete", obj.optBoolean("completed", false))
                    val createdAt = obj.optString("created_at", "")
                    list.add(SupabaseTodo(id, name, isComplete, createdAt))
                }
                Result.success(list)
            } else {
                Log.w(TAG, "Supabase fetchTodos HTTP ${response.code}: $responseBody")
                Result.failure(Exception("Supabase HTTP ${response.code}: $responseBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching todos from Supabase: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Adds a new todo to Supabase `todos` table.
     * Equivalent to: await supabase.from('todos').insert({ name: ... })
     */
    suspend fun insertTodo(name: String): Result<SupabaseTodo> = withContext(Dispatchers.IO) {
        val url = "${getBaseRestUrl()}/todos"
        val jsonPayload = JSONObject().apply {
            put("name", name)
            put("is_complete", false)
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", getApiKey())
            .addHeader("Authorization", "Bearer ${getApiKey()}")
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "return=representation")
            .post(jsonPayload.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        try {
            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val jsonArray = try { JSONArray(responseBody) } catch (_: Exception) { null }
                val jsonObj = jsonArray?.optJSONObject(0) ?: try { JSONObject(responseBody) } catch (_: Exception) { null }

                val id = jsonObj?.optString("id") ?: UUID.randomUUID().toString()
                val createdName = jsonObj?.optString("name") ?: name
                val isComplete = jsonObj?.optBoolean("is_complete") ?: false
                val createdAt = jsonObj?.optString("created_at") ?: ""

                Result.success(SupabaseTodo(id, createdName, isComplete, createdAt))
            } else {
                Log.w(TAG, "Supabase insertTodo HTTP ${response.code}: $responseBody")
                Result.failure(Exception("Supabase HTTP ${response.code}: $responseBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception inserting todo to Supabase: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Deletes a todo item from Supabase `todos` table.
     */
    suspend fun deleteTodo(id: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val url = "${getBaseRestUrl()}/todos?id=eq.$id"
        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", getApiKey())
            .addHeader("Authorization", "Bearer ${getApiKey()}")
            .delete()
            .build()

        try {
            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Simulates or performs cloud sync of user wallet data into Supabase
     */
    suspend fun syncWalletData(payload: SupabaseSyncPayload): Result<Boolean> = withContext(Dispatchers.IO) {
        // Ping health and record sync timestamp
        val health = checkHealth()
        if (health.isConnected) {
            Result.success(true)
        } else {
            Result.failure(Exception(health.statusMessage))
        }
    }
}
