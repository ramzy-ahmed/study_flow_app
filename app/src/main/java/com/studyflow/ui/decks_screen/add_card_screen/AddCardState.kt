package com.studyflow.ui.decks_screen.add_card_screen

sealed class AddCardState {
    object Idle : AddCardState()
    object Success : AddCardState()
    data class Error (val message: String): AddCardState()
}