package com.studyflow.ui.study_screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyflow.data.local.entity.CardEntity
import com.studyflow.data.local.entity.SessionResultEntity
import com.studyflow.data.repository.DeckRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudyViewModel @Inject constructor(
    private val repository: DeckRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val mode: StudyScreen.StudyMode = checkNotNull(savedStateHandle["mode"])
    private val deckId: Int = checkNotNull(savedStateHandle["selectedDeck"])

    private val _deckWithCards = repository.getDeckWithCards(deckId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val cards: StateFlow<List<CardEntity>> = _deckWithCards
        .map { it?.cards ?: emptyList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex = _currentIndex.asStateFlow()

    private val _gotItCount = MutableStateFlow(0)
    val gotItCount = _gotItCount.asStateFlow()

    private val _missedCount = MutableStateFlow(0)
    val missedCount = _missedCount.asStateFlow()

    private val _correctAnswers = MutableStateFlow(0)
    val correctAnswers = _correctAnswers.asStateFlow()

    private val _incorrectAnswers = MutableStateFlow(0)
    val incorrectAnswers = _incorrectAnswers.asStateFlow()

    private val _showBack = MutableStateFlow(false)
    val showBack = _showBack.asStateFlow()

    private val _isFinished = MutableSharedFlow<Unit>(replay = 0)
    val isFinished = _isFinished.asSharedFlow()

    private var startTime = System.currentTimeMillis()

    fun onFlipCard() {
        _showBack.value = !_showBack.value
    }

    fun nextCard() {
        if (_currentIndex.value < cards.value.size - 1) {
            _currentIndex.value++
            _showBack.value = false
        } else {
            finishSession()
        }
    }

    private fun finishSession() {
        viewModelScope.launch {
            val total = cards.value.size
            val correct = _correctAnswers.value + _gotItCount.value
            val incorrect = _incorrectAnswers.value + _missedCount.value
            val score = if (total > 0) (correct.toFloat() / total * 100).toInt() else 0
            val timeSpent = (System.currentTimeMillis() - startTime) / 1000

            val session = SessionResultEntity(
                deckId = deckId,
                correctCount = correct,
                incorrectCount = incorrect,
                totalCards = total,
                score = score,
                timeSpent = timeSpent
            )
            repository.insertSessionResult(session)
            _isFinished.emit(Unit)
        }
    }

    fun previousCard() {
        if (_currentIndex.value > 0) {
            _currentIndex.value--
            _showBack.value = false
        }
    }

    fun resetSession() {
        _currentIndex.value = 0
        _gotItCount.value = 0
        _missedCount.value = 0
        _correctAnswers.value = 0
        _incorrectAnswers.value = 0
        _showBack.value = false
        startTime = System.currentTimeMillis()
    }

    fun updateCardProgress(feedback: String) {
        val currentCard = cards.value.getOrNull(_currentIndex.value) ?: return

        viewModelScope.launch {
            val updatedCard = when (feedback) {
                "Easy" -> {
                    _gotItCount.value++
                    currentCard.copy(
                        difficultyLevel = "Easy",
                        masteryScore = (currentCard.masteryScore + 20).coerceAtMost(100),
                        interval = if (currentCard.interval == 0) 1 else currentCard.interval * 2,
                        lastReviewed = System.currentTimeMillis()
                    )
                }
                "Good" -> {
                    _gotItCount.value++
                    currentCard.copy(
                        difficultyLevel = "Medium",
                        masteryScore = (currentCard.masteryScore + 10).coerceAtMost(100),
                        interval = if (currentCard.interval == 0) 1 else (currentCard.interval * 1.5).toInt(),
                        lastReviewed = System.currentTimeMillis()
                    )
                }
                "Hard" -> {
                    _missedCount.value++
                    currentCard.copy(
                        difficultyLevel = "Hard",
                        masteryScore = (currentCard.masteryScore - 10).coerceAtLeast(0),
                        interval = 0,
                        lastReviewed = System.currentTimeMillis()
                    )
                }
                else -> currentCard
            }
            repository.updateCard(updatedCard)
            nextCard()
        }
    }

    fun submitAnswer(userAnswer: String): Boolean {
        val currentCard = cards.value.getOrNull(_currentIndex.value) ?: return false
        val isCorrect = currentCard.answer.trim().equals(userAnswer.trim(), ignoreCase = true)

        if (isCorrect) {
            _correctAnswers.value++
            updateCardProgressAfterQuiz(currentCard, true)
        } else {
            _incorrectAnswers.value++
            updateCardProgressAfterQuiz(currentCard, false)
        }
        return isCorrect
    }

    private fun updateCardProgressAfterQuiz(card: CardEntity, isCorrect: Boolean) {
        viewModelScope.launch {
            val updatedCard = if (isCorrect) {
                card.copy(
                    masteryScore = (card.masteryScore + 15).coerceAtMost(100),
                    lastReviewed = System.currentTimeMillis()
                )
            } else {
                card.copy(
                    masteryScore = (card.masteryScore - 10).coerceAtLeast(0),
                    lastReviewed = System.currentTimeMillis()
                )
            }
            repository.updateCard(updatedCard)
            if (isCorrect) nextCard()
        }
    }
}