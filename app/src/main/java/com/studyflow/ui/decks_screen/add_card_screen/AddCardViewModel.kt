package com.studyflow.ui.decks_screen.add_card_screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyflow.data.local.entity.CardEntity
import com.studyflow.data.repository.DeckRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AddCardViewModel @Inject constructor(
    private val repository: DeckRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val deckId: Int = checkNotNull(savedStateHandle["deckId"])
    private val _uiState = MutableStateFlow<AddCardState>(AddCardState.Idle)

    val uiState: StateFlow<AddCardState> = _uiState

    private val _question = MutableStateFlow("")
    val question: StateFlow<String> = _question

    private val _answer = MutableStateFlow("")
    val answer: StateFlow<String> = _answer

    fun onQuestionChanged(value: String) {
        _question.value = value
    }

    fun onAnswerChanged(value: String) {
        _answer.value = value
    }

    fun saveCard() {
        if (_question.value.isBlank()){
            _uiState.value = AddCardState.Error("The Question can't be empty")
            return
        }
        if ( _answer.value.isBlank()){
            _uiState.value = AddCardState.Error("The Answer can't be empty")
            return
        }

        viewModelScope.launch {
            val card = CardEntity(
                deckId = deckId,
                question = _question.value,
                answer = _answer.value
            )

            repository.insertCardsDirectly(listOf(card))
            _uiState.value = AddCardState.Success
        }
    }
}