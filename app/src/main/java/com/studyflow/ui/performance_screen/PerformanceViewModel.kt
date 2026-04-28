package com.studyflow.ui.performance_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyflow.data.local.entity.SessionResultEntity
import com.studyflow.data.repository.DeckRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class PerformanceViewModel @Inject constructor(
    private val repository: DeckRepository
) : ViewModel() {
    private val sessionResults = repository.getAllSessionResults()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val decksWithCards = repository.getAllDecksWithCards()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val totalCardsLearned: StateFlow<Int> = sessionResults.map { sessions ->
        sessions.sumOf { it.correctCount }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val totalTimeSpent: StateFlow<Long> = sessionResults.map { sessions ->
        sessions.sumOf { it.timeSpent }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0L)

    val averageAccuracy: StateFlow<Int> = sessionResults.map { sessions ->
        if (sessions.isEmpty()) 0
        else (sessions.sumOf { it.score } / sessions.size)
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val masteredCardsCount: StateFlow<Int> = decksWithCards.map { list ->
        list.sumOf { it.cards.count { card -> card.masteryScore >= 80 } }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val currentStreak: StateFlow<Int> = sessionResults.map { sessions ->
        calculateStreak(sessions)
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val weeklyGoalProgress: StateFlow<Int> = sessionResults.map { sessions ->
        val goal = 100
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        val startOfWeek = calendar.timeInMillis

        val current = sessions.filter { it.date >= startOfWeek }.sumOf { it.correctCount }
        if (current >= goal) 100 else (current * 100 / goal)
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val overallMastery: StateFlow<Int> = decksWithCards.map { list ->
        val totalCards = list.sumOf { it.cards.size }
        if (totalCards == 0) 0
        else {
            val totalMastered = list.sumOf { it.cards.count { card -> card.masteryScore >= 80 } }
            (totalMastered * 100 / totalCards)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val deckStats: StateFlow<List<PerformanceStat>> = decksWithCards.map { list ->
        list.map { deckWithCards ->
            val total = deckWithCards.cards.size
            val mastered = deckWithCards.cards.count { it.masteryScore >= 80 }
            val percentage = if (total == 0) 0 else (mastered * 100 / total)
            PerformanceStat(
                deckId = deckWithCards.deck.id,
                title = deckWithCards.deck.title,
                totalCards = total,
                masteredCards = mastered,
                masteryPercentage = percentage,
                color = null // DeckEntity doesn't have a color field
            )
        }.sortedByDescending { it.masteryPercentage }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private fun calculateStreak(sessions: List<SessionResultEntity>): Int {
        if (sessions.isEmpty()) return 0
        val sortedDates = sessions.map {
            val cal = Calendar.getInstance()
            cal.timeInMillis = it.date
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.distinct().sortedDescending()

        var streak = 0
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        var currentCheck = today.timeInMillis

        for (date in sortedDates) {
            if (date == currentCheck || date == currentCheck - 86400000) {
                streak++
                currentCheck = date
            } else if (date < currentCheck - 86400000) {
                break
            }
        }
        return streak
    }
}