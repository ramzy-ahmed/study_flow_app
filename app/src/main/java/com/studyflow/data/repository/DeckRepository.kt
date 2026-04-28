package com.studyflow.data.repository

import com.studyflow.data.local.dao.DeckDao
import com.studyflow.data.local.entity.CardEntity
import com.studyflow.data.local.entity.DeckEntity
import com.studyflow.data.local.entity.DeckWithCards
import com.studyflow.data.local.entity.SessionResultEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeckRepository @Inject constructor(private val dao: DeckDao) {

    fun getAllDecksWithCards(): Flow<List<DeckWithCards>> {
        return dao.getAllDecksWithCards()
    }

    suspend fun insertDeckWithCards(deck: DeckEntity, cards: List<CardEntity>) {
        val deckId = dao.insertDeck(deck)
        val cardsWithDeckId = cards.map {
            it.copy(deckId = deckId.toInt())
        }
        dao.insertCards(cardsWithDeckId)
    }

    suspend fun insertCardsDirectly(cards: List<CardEntity>) {
        dao.insertCards(cards)
    }

    fun getDeckWithCards(deckId: Int): Flow<DeckWithCards?> {
        return dao.getDeckWithCards(deckId)
    }

    suspend fun updateDeck(deck: DeckEntity) {
        dao.updateDeck(deck)
    }

    suspend fun deleteDeck(deck: DeckEntity){
        dao.deleteDeck(deck)
    }

    suspend fun deleteCard(card: CardEntity) {
        dao.deleteCard(card)
    }

    fun searchDecks(query: String): Flow<List<DeckEntity>> {
        return dao.searchDecks(query)
    }

    fun searchDecksWithCards(query: String): Flow<List<DeckWithCards>> {
        return dao.searchDecksWithCards(query)
    }

    // --- Progress Tracking ---
    suspend fun updateCard(card: CardEntity) {
        dao.updateCard(card)
    }

    suspend fun insertSessionResult(session: SessionResultEntity) {
        dao.insertSessionResult(session)
    }

    fun getAllSessionResults(): Flow<List<SessionResultEntity>> {
        return dao.getAllSessionResults()
    }

    fun getAverageScoreForDeck(deckId: Int): Flow<Double?> {
        return dao.getAverageScoreForDeck(deckId)
    }

    fun getTotalCardsLearned(): Flow<Int> {
        return dao.getTotalCardsLearned()
    }

    suspend fun getLatestSessionForResult(deckId: Int): SessionResultEntity? {
        return dao.getLatestSessionForResult(deckId)
    }
}
