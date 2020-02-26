package com.quizlingo.quizlingo.persistence

import android.content.Context
import androidx.room.*
import com.quizlingo.quizlingo.businesslogic.AllDecksGetter
import com.quizlingo.quizlingo.businesslogic.DeckInfoGetter
import com.quizlingo.quizlingo.businesslogic.DeckSaver
import com.quizlingo.quizlingo.businesslogic.Deck as BusinessDeck
import com.quizlingo.quizlingo.businesslogic.Card as BusinessCard

class PersistenceDatabase(context: Context): AllDecksGetter, DeckInfoGetter, DeckSaver {

    private val database =
        AppDatabase.getDatabase(
            context
        )
    private val cardsDao = database.cardsDao()
    private val decksDao = database.decksDao()

    @Entity(tableName = "decks")
    data class Deck(
        @PrimaryKey(autoGenerate = true) val id: Long,
        @ColumnInfo(name = "deck_title") var deckTitle: String,
        @ColumnInfo(name = "deck_description") var deckDescription: String,
        @ColumnInfo(name = "deck_card_count") var deckCardCount: Int
    )

    @Entity(tableName = "cards")
    data class Card(
        @PrimaryKey(autoGenerate = true) val id: Long,
        @ColumnInfo(name = "deck_id") val deckId: Long,
        @ColumnInfo(name = "card_prompt") var cardPrompt: String,
        @ColumnInfo(name = "card_text") var cardText: String
    )

    @Dao
    interface DecksDao {
        @Query("SELECT * FROM decks ORDER BY id ASC")
        suspend fun getDecks(): List<Deck>

        @Query("SELECT * FROM decks WHERE id LIKE :deckId")
        suspend fun getDeckById(deckId: Long): Deck

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertDeck(deck: Deck): Long

        @Update
        suspend fun updateDeck(deck: Deck)

        @Delete
        suspend fun deleteDeck(deck: Deck)
    }

    @Dao
    interface CardsDao {
        @Query("SELECT * FROM cards WHERE deck_id LIKE :deckId ORDER BY id ASC")
        suspend fun getCardsByDeck(deckId: Long): List<Card>

        @Insert(onConflict =  OnConflictStrategy.REPLACE)
        suspend fun insertMany(card: List<Card>)

        @Update
        suspend fun updateMany(cards: List<Card>)

        @Delete
        suspend fun deleteMany(cards: List<Card>)

        @Transaction
        suspend fun updateCardsForDeck(deckId: Long, cards: List<Card>) {
            val inDb = getCardsByDeck(deckId)

            val toDelete = inDb - cards
            val toAdd = cards - inDb
            val toUpdate = cards - toAdd

            deleteMany(toDelete)
            insertMany(toAdd)
            updateMany(toUpdate)
        }
    }

    @Database(entities = [Deck::class, Card::class], version = 1)
    abstract class AppDatabase: RoomDatabase() {
        abstract fun decksDao(): DecksDao
        abstract fun cardsDao(): CardsDao

        companion object {
            private var instance: AppDatabase? = null
            fun getDatabase(context: Context): AppDatabase {
                var tempInstance =
                    instance
                if(tempInstance != null) {
                    return tempInstance
                }
                synchronized(this) {
                    tempInstance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "decks.db").build()
                    instance = tempInstance
                    return tempInstance!!
                }
            }
        }
    }

    override suspend fun getAllDecks(): List<BusinessDeck> {
        return decksDao.getDecks().map{ dbDeck ->
            val cards = cardsDao.getCardsByDeck(dbDeck.id).map{ dbCard ->
                BusinessCard(dbCard.id, dbCard.deckId, dbCard.cardPrompt, dbCard.cardText)
            }
            BusinessDeck(
                dbDeck.id,
                dbDeck.deckTitle,
                dbDeck.deckDescription,
                cards,
                dbDeck.deckCardCount
            )}
    }

    override suspend fun getDeckInfo(deckId: Long): BusinessDeck {
        val dbDeck = decksDao.getDeckById(deckId)
        val cards = cardsDao.getCardsByDeck(dbDeck.id).map {dbCard ->
            BusinessCard(dbCard.id, dbCard.deckId, dbCard.cardPrompt, dbCard.cardText)
        }
        return BusinessDeck(
            dbDeck.id,
            dbDeck.deckTitle,
            dbDeck.deckDescription,
            cards,
            dbDeck.deckCardCount
        )
    }

    override suspend fun saveDeck(deck: BusinessDeck) {
        val dbDeck =
            Deck(
                deck.deckId,
                deck.title,
                deck.description,
                deck.cardCount
            )
        var deckId = dbDeck.id
        if(deck.deckId == 0L) {
            deckId = decksDao.insertDeck(dbDeck)
        } else {
            decksDao.updateDeck(dbDeck)
        }

        val dbCards = deck.cards.map{card ->
            Card(
                card.id,
                deckId,
                card.prompt,
                card.answer
            )
        }

        cardsDao.updateCardsForDeck(deck.deckId, dbCards)

    }

    override suspend fun deleteDeck(deck: BusinessDeck) {
        val dbDeck =
            Deck(
                deck.deckId,
                deck.title,
                deck.description,
                deck.cardCount
            )
        val dbCards = deck.cards.map{card ->
            Card(
                card.id,
                card.deckId,
                card.prompt,
                card.answer
            )
        }

        cardsDao.deleteMany(dbCards)
        decksDao.deleteDeck(dbDeck)
    }

}