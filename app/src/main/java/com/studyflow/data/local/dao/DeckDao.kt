package com.studyflow.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.studyflow.data.local.entity.CardEntity
import com.studyflow.data.local.entity.DeckEntity
import com.studyflow.data.local.entity.DeckWithCards
import com.studyflow.data.local.entity.SessionResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeckDao {

    @Insert
    suspend fun insertDeck(deck: DeckEntity): Long

    @Insert
    suspend fun insertCards(cards: List<CardEntity>)

    @Update
    suspend fun updateDeck(deck: DeckEntity)

    @Transaction
    @Query("SELECT * FROM decks ORDER BY id DESC")
    fun getAllDecks(): Flow<List<DeckEntity>>

    @Transaction
    @Query("SELECT * FROM decks ORDER BY id DESC")
    fun getAllDecksWithCards(): Flow<List<DeckWithCards>>

    @Transaction
    @Query("SELECT * FROM decks")
    suspend fun getDecksWithCards(): List<DeckWithCards>

    @Transaction
    @Query("SELECT * FROM decks WHERE id = :deckId")
    fun getDeckWithCards(deckId: Int): Flow<DeckWithCards?>

    @Delete
    suspend fun deleteDeck(deck: DeckEntity)

    @Delete
    suspend fun deleteCard(card: CardEntity)

    @Query("DELETE FROM decks")
    suspend fun deleteAllDecks()

    @Transaction
    @Query("""
    SELECT * FROM decks 
    WHERE title LIKE '%' || :query || '%' 
    ORDER BY id DESC
""")
    fun searchDecks(query: String): Flow<List<DeckEntity>>

    @Transaction
    @Query("""
    SELECT * FROM decks 
    WHERE title LIKE '%' || :query || '%' 
    ORDER BY id DESC
""")
    fun searchDecksWithCards(query: String): Flow<List<DeckWithCards>>

    // --- Statistics & Progress Tracking ---

    @Update
    suspend fun updateCard(card: CardEntity)

    @Insert
    suspend fun insertSessionResult(session: SessionResultEntity)

    @Query("SELECT * FROM session_results ORDER BY date DESC")
    fun getAllSessionResults(): Flow<List<SessionResultEntity>>

    @Query("SELECT AVG(score) FROM session_results WHERE deckId = :deckId")
    fun getAverageScoreForDeck(deckId: Int): Flow<Double?>

    @Query("SELECT COUNT(*) FROM cards WHERE masteryScore >= 80")
    fun getTotalCardsLearned(): Flow<Int>
    
    @Query("SELECT * FROM session_results WHERE deckId = :deckId ORDER BY date DESC LIMIT 1")
    suspend fun getLatestSessionForResult(deckId: Int): SessionResultEntity?
}
