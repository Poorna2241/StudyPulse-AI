package com.yourgroup.studypulseai.network

import android.util.Log
import com.yourgroup.studypulseai.network.models.SDeck
import com.yourgroup.studypulseai.network.models.SFlashcard
import com.yourgroup.studypulseai.network.models.SQuizResult
import com.yourgroup.studypulseai.network.models.SChallenge
import com.yourgroup.studypulseai.network.models.SChallengeParticipant
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.realtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object SupabaseRepo {
    private const val TAG = "SupabaseRepo"
    private val scope = CoroutineScope(Dispatchers.IO)

    interface DecksCallback { fun onResult(decks: List<SDeck>) }
    interface SaveCallback { fun onResult(id: Int?) }
    interface StatusCallback { fun onResult(success: Boolean) }
    interface ParticipantsCallback { fun onUpdate(participants: List<SChallengeParticipant>) }
    interface ChallengeCallback { fun onResult(challenge: SChallenge?) }
    interface StatusUpdateCallback { fun onStatusUpdate(status: String) }
    interface ParticipantCallback { fun onResult(participant: SChallengeParticipant?) }

    private val activeChannels = mutableMapOf<String, RealtimeChannel>()
    private val channelJobs = mutableMapOf<String, Job>()

    @JvmStatic
    fun getParticipantStatus(challengeId: Int, callback: ParticipantCallback) {
        scope.launch {
            try {
                val userId = SupabaseManager.client.auth.currentSessionOrNull()?.user?.id ?: return@launch
                val participant = SupabaseManager.client.postgrest["challenge_participants"]
                    .select {
                        filter {
                            eq("challenge_id", challengeId)
                            eq("user_id", userId)
                        }
                    }.decodeSingleOrNull<SChallengeParticipant>()
                withContext(Dispatchers.Main) { callback.onResult(participant) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { callback.onResult(null) }
            }
        }
    }

    @JvmStatic
    fun saveDeck(title: String, background: String, callback: SaveCallback) {
        scope.launch {
            try {
                val userId = SupabaseManager.client.auth.currentSessionOrNull()?.user?.id ?: return@launch
                val deck = SDeck(user_id = userId, title = title, background_image = background, created_at = System.currentTimeMillis())
                val response = SupabaseManager.client.postgrest["decks"].insert(deck) { select() }.decodeSingle<SDeck>()
                withContext(Dispatchers.Main) { callback.onResult(response.id) }
            } catch (e: Exception) {
                Log.e(TAG, "saveDeck failed", e)
                withContext(Dispatchers.Main) { callback.onResult(null) }
            }
        }
    }

    @JvmStatic
    fun saveFlashcards(flashcards: List<SFlashcard>, callback: StatusCallback) {
        scope.launch {
            try {
                SupabaseManager.client.postgrest["flashcards"].insert(flashcards)
                withContext(Dispatchers.Main) { callback.onResult(true) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { callback.onResult(false) }
            }
        }
    }

    @JvmStatic
    fun fetchDecks(callback: DecksCallback) {
        scope.launch {
            try {
                val userId = SupabaseManager.client.auth.currentSessionOrNull()?.user?.id ?: return@launch
                val decks = SupabaseManager.client.postgrest["decks"].select { filter { eq("user_id", userId) } }.decodeList<SDeck>()
                withContext(Dispatchers.Main) { callback.onResult(decks) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { callback.onResult(emptyList()) }
            }
        }
    }

    interface ChallengesCallback { fun onResult(challenges: List<SChallenge>) }

    @JvmStatic
    fun fetchUserChallenges(callback: ChallengesCallback) {
        scope.launch {
            try {
                val userId = SupabaseManager.client.auth.currentSessionOrNull()?.user?.id ?: return@launch
                val participantEntries = SupabaseManager.client.postgrest["challenge_participants"]
                    .select { filter { eq("user_id", userId) } }
                    .decodeList<SChallengeParticipant>()
                val joinedIds = participantEntries.mapNotNull { it.challenge_id }.toSet()
                val allChallenges = SupabaseManager.client.postgrest["challenges"].select().decodeList<SChallenge>()
                val userChallenges = allChallenges.filter { it.host_id == userId || joinedIds.contains(it.id) }
                withContext(Dispatchers.Main) { callback.onResult(userChallenges) }
            } catch (e: Exception) {
                Log.e(TAG, "fetchUserChallenges failed", e)
                withContext(Dispatchers.Main) { callback.onResult(emptyList()) }
            }
        }
    }

    @JvmStatic
    fun saveQuizResult(deckTitle: String, score: Int, total: Int, callback: StatusCallback) {
        scope.launch {
            try {
                val userId = SupabaseManager.client.auth.currentSessionOrNull()?.user?.id ?: return@launch
                val result = SQuizResult(user_id = userId, deck_title = deckTitle, score = score, total_questions = total, timestamp = System.currentTimeMillis())
                SupabaseManager.client.postgrest["quiz_results"].insert(result)
                withContext(Dispatchers.Main) { callback.onResult(true) }
            } catch (e: Exception) {
                Log.e(TAG, "saveQuizResult failed", e)
                withContext(Dispatchers.Main) { callback.onResult(false) }
            }
        }
    }

    @JvmStatic
    fun createChallenge(deckId: Int, title: String, notes: String, count: Int, diff: String, startTime: Long, durationMins: Int, code: String, quizJson: String?, callback: SaveCallback) {
        scope.launch {
            try {
                val userId = SupabaseManager.client.auth.currentSessionOrNull()?.user?.id ?: return@launch
                Log.d(TAG, "Attempting to create challenge with JSON size: ${quizJson?.length ?: 0}")
                
                val challenge = SChallenge(
                    host_id = userId,
                    deck_id = deckId,
                    deck_title = title,
                    deck_notes = notes,
                    question_count = count,
                    difficulty = diff,
                    start_time = startTime,
                    duration_mins = durationMins,
                    challenge_code = code,
                    quiz_json = quizJson
                )
                
                val response = SupabaseManager.client.postgrest["challenges"].insert(challenge) { select() }.decodeSingle<SChallenge>()
                Log.d(TAG, "Challenge created successfully: ${response.id}")

                joinChallenge(response.id!!, SupabaseAuthHelper.getCurrentUserName() ?: "Host", object : StatusCallback {
                    override fun onResult(success: Boolean) {
                        Log.d(TAG, "Host joined challenge: $success")
                    }
                })
                withContext(Dispatchers.Main) { callback.onResult(response.id) }
            } catch (e: Exception) {
                Log.e(TAG, "createChallenge failed: ${e.message}", e)
                withContext(Dispatchers.Main) { callback.onResult(null) }
            }
        }
    }

    @JvmStatic
    fun joinChallenge(challengeId: Int, userName: String, callback: StatusCallback) {
        scope.launch {
            try {
                val userId = SupabaseManager.client.auth.currentSessionOrNull()?.user?.id ?: return@launch
                val participant = SChallengeParticipant(challenge_id = challengeId, user_id = userId, user_name = userName)
                SupabaseManager.client.postgrest["challenge_participants"].insert(participant)
                withContext(Dispatchers.Main) { callback.onResult(true) }
            } catch (e: Exception) {
                Log.e(TAG, "joinChallenge failed", e)
                withContext(Dispatchers.Main) { callback.onResult(false) }
            }
        }
    }

    @JvmStatic
    fun getChallengeByCode(code: String, callback: ChallengeCallback) {
        scope.launch {
            try {
                val challenge = SupabaseManager.client.postgrest["challenges"].select { filter { eq("challenge_code", code) } }.decodeSingleOrNull<SChallenge>()
                withContext(Dispatchers.Main) { callback.onResult(challenge) }
            } catch (e: Exception) {
                Log.e(TAG, "getChallengeByCode failed", e)
                withContext(Dispatchers.Main) { callback.onResult(null) }
            }
        }
    }

    @JvmStatic
    fun getChallengeById(id: Int, callback: ChallengeCallback) {
        scope.launch {
            try {
                val challenge = SupabaseManager.client.postgrest["challenges"].select { filter { eq("id", id) } }.decodeSingleOrNull<SChallenge>()
                withContext(Dispatchers.Main) { callback.onResult(challenge) }
            } catch (e: Exception) {
                Log.e(TAG, "getChallengeById failed", e)
                withContext(Dispatchers.Main) { callback.onResult(null) }
            }
        }
    }

    @JvmStatic
    fun listenToParticipants(challengeId: Int, callback: ParticipantsCallback) {
        val channelId = "participants_$challengeId"
        cleanupChannel(channelId)

        scope.launch {
            try {
                val initial = SupabaseManager.client.postgrest["challenge_participants"].select { filter { eq("challenge_id", challengeId) } }.decodeList<SChallengeParticipant>()
                withContext(Dispatchers.Main) { callback.onUpdate(initial) }
            } catch (e: Exception) {}

            val channel = SupabaseManager.client.realtime.channel(channelId)
            activeChannels[channelId] = channel
            
            val job = channel.postgresChangeFlow<PostgresAction>(schema = "public") { table = "challenge_participants" }.onEach {
                try {
                    val updated = SupabaseManager.client.postgrest["challenge_participants"].select { filter { eq("challenge_id", challengeId) } }.decodeList<SChallengeParticipant>()
                    withContext(Dispatchers.Main) { callback.onUpdate(updated) }
                } catch (e: Exception) {}
            }.launchIn(scope)
            
            channelJobs[channelId] = job
            channel.subscribe()
        }
    }

    @JvmStatic
    fun listenToChallengeStatus(challengeId: Int, callback: StatusUpdateCallback) {
        val channelId = "status_$challengeId"
        cleanupChannel(channelId)

        scope.launch {
            val channel = SupabaseManager.client.realtime.channel(channelId)
            activeChannels[channelId] = channel

            val job = channel.postgresChangeFlow<PostgresAction>(schema = "public") { table = "challenges" }.onEach { action ->
                if (action is PostgresAction.Update) {
                    val challenge = action.decodeRecord<SChallenge>()
                    if (challenge.id == challengeId) {
                        withContext(Dispatchers.Main) { callback.onStatusUpdate(challenge.status) }
                    }
                }
            }.launchIn(scope)
            
            channelJobs[channelId] = job
            channel.subscribe()
        }
    }

    private fun cleanupChannel(id: String) {
        channelJobs[id]?.cancel()
        channelJobs.remove(id)
        // We don't unsubscribe here to avoid race conditions with quick re-entry, 
        // but we stop the old flow job to prevent IllegalStateException on re-assign.
    }

    @JvmStatic
    @JvmOverloads
    fun updateChallengeStatus(challengeId: Int, newStatus: String, quizJson: String? = null) {
        scope.launch {
            try {
                if (quizJson != null) {
                    SupabaseManager.client.postgrest["challenges"].update({
                        set("status", newStatus)
                        set("quiz_json", quizJson)
                    }) { filter { eq("id", challengeId) } }
                } else {
                    SupabaseManager.client.postgrest["challenges"].update({
                        set("status", newStatus)
                    }) { filter { eq("id", challengeId) } }
                }
            } catch (e: Exception) {
                Log.e(TAG, "updateChallengeStatus failed", e)
            }
        }
    }

    @JvmStatic
    fun updateParticipantResult(challengeId: Int, score: Int, timeMs: Long) {
        scope.launch {
            try {
                val userId = SupabaseManager.client.auth.currentSessionOrNull()?.user?.id ?: return@launch
                SupabaseManager.client.postgrest["challenge_participants"].update({
                    set("score", score)
                    set("completion_time_ms", timeMs)
                    set("status", "finished")
                }) {
                    filter {
                        eq("challenge_id", challengeId)
                        eq("user_id", userId)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "updateParticipantResult failed", e)
            }
        }
    }

    @JvmStatic
    fun leaveChallenge(challengeId: Int) {
        scope.launch {
            try {
                val userId = SupabaseManager.client.auth.currentSessionOrNull()?.user?.id ?: return@launch
                SupabaseManager.client.postgrest["challenge_participants"].delete {
                    filter {
                        eq("challenge_id", challengeId)
                        eq("user_id", userId)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "leaveChallenge failed", e)
            }
        }
    }
}
