package com.example.data.supabase

import com.example.BuildConfig

/**
 * Supabase configuration constants loaded securely from BuildConfig via Secrets Gradle Plugin (.env).
 * Fallback values are provided to ensure zero runtime disruptions.
 */
object SupabaseConfig {
    const val DEFAULT_URL = "https://hzxbolzwcrnbmdbqppzm.supabase.co"
    const val DEFAULT_PUBLISHABLE_KEY = "sb_publishable_V9neQtuzs34Rv7moPpj9aQ_AuVBbUTk"

    val supabaseUrl: String
        get() = try {
            val key = BuildConfig.SUPABASE_URL
            if (key.isNullOrBlank() || key.contains("MY_")) DEFAULT_URL else key
        } catch (_: Throwable) {
            DEFAULT_URL
        }

    val supabaseKey: String
        get() = try {
            val key = BuildConfig.SUPABASE_KEY
            if (key.isNullOrBlank() || key.contains("MY_")) DEFAULT_PUBLISHABLE_KEY else key
        } catch (_: Throwable) {
            DEFAULT_PUBLISHABLE_KEY
        }
}
