package com.yourgroup.studypulseai.network.models

import kotlinx.serialization.Serializable

@Serializable
data class SDeck(
    val id: Int? = null,
    val user_id: String,
    val title: String,
    val background_image: String,
    val created_at: Long
)

@Serializable
data class SFlashcard(
    val id: Int? = null,
    val deck_id: Int,
    val user_id: String,
    val question: String,
    val answer: String,
    val mastery_level: Int
)

@Serializable
data class SQuizResult(
    val id: Int? = null,
    val user_id: String,
    val deck_title: String,
    val score: Int,
    val total_questions: Int,
    val timestamp: Long
)

@Serializable
data class SChallenge(
    val id: Int? = null,
    val host_id: String,
    val deck_id: Int,
    val deck_title: String,
    val deck_notes: String,
    val question_count: Int,
    val difficulty: String,
    val start_time: Long,
    val duration_mins: Int,
    val challenge_code: String,
    val status: String = "waiting", // waiting, started, finished
    val quiz_json: String? = null
)

@Serializable
data class SChallengeParticipant(
    val id: Int? = null,
    val challenge_id: Int,
    val user_id: String,
    val user_name: String,
    val score: Int = 0,
    val completion_time_ms: Long = 0,
    val status: String = "joined", // joined, playing, finished
    val xp_earned: Int = 0
)
