package com.studyflow.ui.decks_screen.decks_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyflow.data.local.entity.DeckEntity
import com.studyflow.data.repository.DeckRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class DecksViewModel @Inject constructor(
    private val repository: DeckRepository
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val uiState: StateFlow<DecksState> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.getAllDecksWithCards()
            } else {
                repository.searchDecksWithCards(query)
            }
        }
        .map { decks ->
            if (decks.isEmpty()) {
                if (_searchQuery.value.isBlank()) DecksState.Empty
                else DecksState.NoResultsFound
            } else {
                DecksState.Success(decks)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DecksState.Loading
        )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch(){
        _searchQuery.value = ""
    }

    fun deleteDeck(deck: DeckEntity) {
        viewModelScope.launch {
            repository.deleteDeck(deck)
        }
    }
}
