package com.yourgroup.studypulseai.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import android.content.Context
import com.yourgroup.studypulseai.StudyPulseApp

class AndroidSessionManager : SessionManager {
    private val prefs by lazy {
        StudyPulseApp.getInstance().getSharedPreferences("SupabaseAuthPrefs", Context.MODE_PRIVATE)
    }

    override suspend fun saveSession(session: UserSession) {
        try {
            val json = Json.encodeToString(session)
            prefs.edit().putString("user_session", json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun loadSession(): UserSession {
        val json = prefs.getString("user_session", null) ?: throw NoSuchElementException("No session saved")
        return try {
            Json.decodeFromString<UserSession>(json)
        } catch (e: Exception) {
            throw NoSuchElementException("Error decoding session")
        }
    }

    override suspend fun deleteSession() {
        prefs.edit().remove("user_session").apply()
    }
}

object SupabaseManager {
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SupabaseConfig.SUPABASE_URL,
            supabaseKey = SupabaseConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth) {
                sessionManager = AndroidSessionManager()
            }
            install(Postgrest)
            install(Storage)
        }
    }
}
