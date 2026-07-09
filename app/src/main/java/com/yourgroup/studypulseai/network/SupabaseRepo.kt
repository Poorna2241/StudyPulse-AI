package com.yourgroup.studypulseai.network

import android.util.Log
import com.yourgroup.studypulseai.network.models.SDeck
import com.yourgroup.studypulseai.network.models.SFlashcard
import com.yourgroup.studypulseai.network.models.SQuizResult
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object SupabaseRepo {
    private const val TAG = "SupabaseRepo"
    private val scope = CoroutineScope(Dispatchers.IO)

    interface DecksCallback {
        fun onResult(decks: List<SDeck>)
    }

    interface SaveCallback {
        fun onResult(id: Int?)
    }

    interface StatusCallback {
        fun onResult(success: Boolean)
    }

    @JvmStatic
    fun saveDeck(title: String, background: String, callback: SaveCallback) {
        scope.launch {
            try {
                val userId = SupabaseManager.client.auth.currentSessionOrNull()?.user?.id
                if (userId == null) {
                    Log.e(TAG, "saveDeck aborted: no logged-in Supabase user (session is null)")
                    withContext(Dispatchers.Main) {
                        callback.onResult(null)
                    }
                    return@launch
                }

                val deck = SDeck(
                    user_id = userId,
                    title = title,
                    background_image = background,
                    created_at = System.currentTimeMillis()
                )
                val response = SupabaseManager.client.postgrest["decks"].insert(deck) {
                    select()
                }.decodeSingle<SDeck>()

                withContext(Dispatchers.Main) {
                    callback.onResult(response.id)
                }
            } catch (e: Exception) {
                Log.e(TAG, "saveDeck failed", e)
                withContext(Dispatchers.Main) {
                    callback.onResult(null)
                }
            }
        }
    }

    @JvmStatic
    fun saveFlashcards(flashcards: List<SFlashcard>, callback: StatusCallback) {
        scope.launch {
            try {
                SupabaseManager.client.postgrest["flashcards"].insert(flashcards)
                withContext(Dispatchers.Main) {
                    callback.onResult(true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "saveFlashcards failed", e)
                withContext(Dispatchers.Main) {
                    callback.onResult(false)
                }
            }
        }
    }

    @JvmStatic
    fun fetchDecks(callback: DecksCallback) {
        scope.launch {
            try {
                val userId = SupabaseManager.client.auth.currentSessionOrNull()?.user?.id
                if (userId == null) {
                    Log.e(TAG, "fetchDecks aborted: no logged-in Supabase user (session is null)")
                    withContext(Dispatchers.Main) {
                        callback.onResult(emptyList())
                    }
                    return@launch
                }

                val decks = SupabaseManager.client.postgrest["decks"]
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                    }.decodeList<SDeck>()
                withContext(Dispatchers.Main) {
                    callback.onResult(decks)
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetchDecks failed", e)
                withContext(Dispatchers.Main) {
                    callback.onResult(emptyList())
                }
            }
        }
    }

    @JvmStatic
    fun saveQuizResult(deckTitle: String, score: Int, total: Int, callback: StatusCallback) {
        scope.launch {
            try {
                val userId = SupabaseManager.client.auth.currentSessionOrNull()?.user?.id
                if (userId == null) {
                    Log.e(TAG, "saveQuizResult aborted: no logged-in Supabase user (session is null)")
                    withContext(Dispatchers.Main) {
                        callback.onResult(false)
                    }
                    return@launch
                }

                val result = SQuizResult(
                    user_id = userId,
                    deck_title = deckTitle,
                    score = score,
                    total_questions = total,
                    timestamp = System.currentTimeMillis()
                )
                SupabaseManager.client.postgrest["quiz_results"].insert(result)
                withContext(Dispatchers.Main) {
                    callback.onResult(true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "saveQuizResult failed", e)
                withContext(Dispatchers.Main) {
                    callback.onResult(false)
                }
            }
        }
    }
}