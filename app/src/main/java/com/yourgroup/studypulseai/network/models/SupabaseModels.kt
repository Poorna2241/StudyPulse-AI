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
