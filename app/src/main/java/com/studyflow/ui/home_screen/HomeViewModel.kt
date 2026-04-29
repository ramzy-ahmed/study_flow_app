package com.studyflow.ui.home_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyflow.data.dummy.DummyData
import com.studyflow.data.local.entity.DeckWithCards
import com.studyflow.data.repository.DeckRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: DeckRepository
) : ViewModel() {

    val decksWithCards: StateFlow<List<DeckWithCards>> = repository.getAllDecksWithCards()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val recentDecks: StateFlow<List<DeckWithCards>> = decksWithCards.map { list ->
        list.sortedByDescending { it.deck.id }.take(5)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val totalDecksCount: StateFlow<Int> = decksWithCards.map { it.size }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val totalCardsCount: StateFlow<Int> = decksWithCards.map { list ->
        list.sumOf { it.cards.size }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val averageScore: StateFlow<Int> = repository.getAllSessionResults().map { sessions ->
        if (sessions.isEmpty()) 0
        else (sessions.sumOf { it.score } / sessions.size)
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val dueCardsCount: StateFlow<Int> = decksWithCards.map { list ->
        list.sumOf { it.cards.size } / 2
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val weeklyFocusData: StateFlow<List<Float>> = repository.getAllSessionResults().map { sessions ->
        val dailyCounts = MutableList(7) { 0 }
        val calendar = Calendar.getInstance()

        // Find Monday of the current week
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfThisWeek = calendar.timeInMillis

        sessions.forEach { session ->
            if (session.date >= startOfThisWeek) {
                val sessionCal = Calendar.getInstance()
                sessionCal.timeInMillis = session.date
                // Adjust to 0-indexed where 0 is Monday
                var dayIndex = sessionCal.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY
                if (dayIndex < 0) dayIndex += 7
                if (dayIndex in 0..6) {
                    dailyCounts[dayIndex] += session.totalCards
                }
            }
        }

        val max = dailyCounts.maxOrNull()?.takeIf { it > 0 } ?: 1
        dailyCounts.map { it.toFloat() / max }
    }.stateIn(viewModelScope, SharingStarted.Lazily, List(7) { 0f })

    val weeklyGrowth: StateFlow<String> = repository.getAllSessionResults().map { sessions ->
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY

        // This Week
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val thisWeekStart = calendar.timeInMillis

        // Last Week
        calendar.add(Calendar.WEEK_OF_YEAR, -1)
        val lastWeekStart = calendar.timeInMillis
        val lastWeekEnd = thisWeekStart - 1

        val thisWeekTotal = sessions.filter { it.date >= thisWeekStart }.sumOf { it.totalCards }
        val lastWeekTotal = sessions.filter { it.date in lastWeekStart..lastWeekEnd }.sumOf { it.totalCards }

        if (lastWeekTotal == 0) {
            if (thisWeekTotal > 0) "+100%" else "0%"
        } else {
            val growth = ((thisWeekTotal - lastWeekTotal).toFloat() / lastWeekTotal) * 100
            val prefix = if (growth >= 0) "+" else ""
            "${prefix}${growth.toInt()}%"
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, "0%")

    val currentStreak: StateFlow<Int> = repository.getAllSessionResults().map { sessions ->
        if (sessions.isEmpty()) return@map 0

        val calendar = Calendar.getInstance()
        val uniqueDays = sessions.map {
            calendar.timeInMillis = it.date
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.timeInMillis
        }.distinct().sortedDescending()

        if (uniqueDays.isEmpty()) return@map 0

        var streak = 0
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val yesterday = today - 24 * 60 * 60 * 1000

        // Check if the latest session was today or yesterday
        val latestSessionDay = uniqueDays.first()
        if (latestSessionDay < yesterday) return@map 0

        var currentDay = latestSessionDay
        for (day in uniqueDays) {
            if (day == currentDay) {
                streak++
                currentDay -= 24 * 60 * 60 * 1000
            } else {
                break
            }
        }
        streak
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    //
    fun insertDummyData() {
        viewModelScope.launch {
            val dummyData = DummyData.getDummyDecks()
            dummyData.forEach { (deck, cards) ->
                repository.insertDeckWithCards(deck, cards)
            }
        }
    }
}
