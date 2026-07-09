package com.yourgroup.studypulseai.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SupabaseUser(
    val id: String,
    val email: String
)
