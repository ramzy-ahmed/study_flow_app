package com.studyflow.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cards",
    foreignKeys = [
        ForeignKey(
            entity = DeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("deckId")]
)
data class CardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val deckId: Int,
    val question: String,
    val answer: String,
    val difficultyLevel: String = "Medium", // Easy, Medium, Hard
    val lastReviewed: Long = 0L,
    val masteryScore: Int = 0, // 0 to 100
    val interval: Int = 0 // for Spaced Repetition (in days)
)
