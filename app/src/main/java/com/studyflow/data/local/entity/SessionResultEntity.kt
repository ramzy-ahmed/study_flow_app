package com.studyflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_results")
data class SessionResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val deckId: Int,
    val correctCount: Int,
    val incorrectCount: Int,
    val totalCards: Int,
    val score: Int,
    val date: Long = System.currentTimeMillis(),
    val timeSpent: Long // in seconds
)
