package com.studyflow.ui.decks_screen.deck_details_screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyflow.data.local.entity.CardEntity
import com.studyflow.data.local.entity.DeckEntity
import com.studyflow.data.local.entity.DeckWithCards
import com.studyflow.data.repository.DeckRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeckDetailsViewModel @Inject constructor(
    private val repository: DeckRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val deckId: Int = checkNotNull(savedStateHandle["deckId"])
    private val _deckDeleted = MutableSharedFlow<Unit>()
    val deckDeleted = _deckDeleted.asSharedFlow()

    val deckWithCards: StateFlow<DeckWithCards?> =
        repository.getDeckWithCards(deckId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    val masteryPercentage: StateFlow<Int> = deckWithCards
        .map { data ->
            if (data == null || data.cards.isEmpty()) 0
            else {
                val totalMastery = data.cards.sumOf { it.masteryScore }
                totalMastery / data.cards.size
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun updateDeck(deck: DeckEntity) {
        viewModelScope.launch {
            repository.updateDeck(deck)
        }
    }

    fun updateCard(card: CardEntity) {
        viewModelScope.launch {
            repository.updateCard(card)
        }
    }
    fun deleteDeck(deck: DeckEntity) {
        viewModelScope.launch {
            repository.deleteDeck(deck)
            _deckDeleted.emit(Unit)
        }
    }
    fun deleteCard(card: CardEntity) {
        viewModelScope.launch {
            repository.deleteCard(card)
        }
    }
}