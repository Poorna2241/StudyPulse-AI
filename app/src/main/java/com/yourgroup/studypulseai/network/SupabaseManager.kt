package com.yourgroup.studypulseai.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.realtime.Realtime
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import android.content.Context
import com.yourgroup.studypulseai.StudyPulseApp
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    private val scope = CoroutineScope(Dispatchers.Main)

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SupabaseConfig.SUPABASE_URL,
            supabaseKey = SupabaseConfig.SUPABASE_ANON_KEY
        ) {
            httpEngine = OkHttp.create()
            install(Auth) {
                sessionManager = AndroidSessionManager()
            }
            install(Postgrest)
            install(Storage)
            install(Realtime)
        }
    }

    @JvmStatic
    fun refreshSessionIfNeeded() {
        scope.launch {
            try {
                // Manually trigger a refresh to prevent token expiration resets
                client.auth.importSession(client.auth.currentSessionOrNull() ?: return@launch)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
