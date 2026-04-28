package com.studyflow.ui.decks_screen.deck_details_screen

import com.studyflow.data.local.entity.CardEntity

sealed class DeckDetailsState {
    object Idle : DeckDetailsState()
    object Loading : DeckDetailsState()
    data class Success(val decks: List<CardEntity>) : DeckDetailsState()
    class Error(val message: String): DeckDetailsState()
}