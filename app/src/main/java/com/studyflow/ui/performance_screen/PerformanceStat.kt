package com.studyflow.ui.performance_screen

data class PerformanceStat(
    val deckId: Int,
    val title: String,
    val totalCards: Int,
    val masteredCards: Int,
    val masteryPercentage: Int,
    val color: String? = null
)