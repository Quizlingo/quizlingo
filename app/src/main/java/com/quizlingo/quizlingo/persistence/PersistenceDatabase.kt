package com.quizlingo.quizlingo.persistence

import android.content.Context
import androidx.room.*
import com.quizlingo.quizlingo.businesslogic.AllDecksGetter
import com.quizlingo.quizlingo.businesslogic.DeckSaver
import com.quizlingo.quizlingo.businesslogic.Deck as BusinessDeck
import com.quizlingo.quizlingo.businesslogic.Card as BusinessCard

class PersistenceDatabase(context: Context): AllDecksGetter, DeckSaver {

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
        @ColumnInfo(name = "deck_description") var deckDescription: String
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
        suspend fun insert(card: Card): Long

        @Update
        suspend fun updateMany(cards: List<Card>)

        @Delete
        suspend fun deleteMany(cards: List<Card>)

        @Transaction
        suspend fun updateCardsForDeck(deckId: Long, cards: List<Card>): List<Card> {
            val inDb = getCardsByDeck(deckId)

            val toDelete = inDb - cards
            val toAdd = cards - inDb
            val toUpdate = cards - toAdd

            deleteMany(toDelete)

            val added = mutableListOf<Card>()
            toAdd.forEach{
                val newId = insert(it)
                added.add(Card(newId, it.deckId, it.cardPrompt, it.cardText))
            }

            updateMany(toUpdate)

            return (toUpdate + toAdd).sortedBy { it.id }
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
                cards
            )}
    }

    override suspend fun saveDeck(deck: BusinessDeck): BusinessDeck {
        val dbDeck =
            Deck(
                deck.deckId,
                deck.title,
                deck.description
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

        val cards = cardsDao.updateCardsForDeck(deck.deckId, dbCards).map{card-> BusinessCard(card.id, deckId, card.cardPrompt, card.cardText)}

        return BusinessDeck(deckId, deck.title, deck.description, cards)
    }

    override suspend fun deleteDeck(deck: BusinessDeck) {
        val dbDeck =
            Deck(
                deck.deckId,
                deck.title,
                deck.description
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