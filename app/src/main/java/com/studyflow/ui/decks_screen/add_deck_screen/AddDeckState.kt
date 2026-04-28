package com.studyflow.ui.decks_screen.add_deck_screen

sealed class AddDeckState {
    object Idle : AddDeckState()
    object Success : AddDeckState()
    data class Error (val message: String): AddDeckState()
}