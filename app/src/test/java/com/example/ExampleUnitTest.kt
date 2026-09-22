package com.example

import com.example.data.supabase.SupabaseConfig
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for app models and Supabase configuration.
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun supabaseConfig_hasValidUrlAndKey() {
    val url = SupabaseConfig.supabaseUrl
    val key = SupabaseConfig.supabaseKey

    assertTrue("Supabase URL should start with https://", url.startsWith("https://"))
    assertTrue("Supabase URL should contain supabase.co", url.contains("supabase.co"))
    assertTrue("Supabase key should not be empty", key.isNotBlank())
    assertTrue("Supabase key should start with sb_publishable_", key.startsWith("sb_publishable_"))
  }
}
