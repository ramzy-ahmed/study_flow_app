package com.studyflow.ui.decks_screen.decks_screen

import com.studyflow.data.local.entity.DeckWithCards

sealed class DecksState {
    object Idle
    object Loading : DecksState()
    object Empty : DecksState()
    object NoResultsFound: DecksState()
    data class Success(val decks: List<DeckWithCards>) : DecksState()
    data class Error(val message: String) : DecksState()
}
