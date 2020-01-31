package com.quizlingo.verticalprototype

import androidx.room.*

class DatabaseComponent {

    @Entity(tableName = "decks")
    data class Deck(
        @PrimaryKey val id: Int,
        @ColumnInfo(name = "deck_name") val deckName: String,
        @ColumnInfo(name = "deck_description") val deckDescription: String
    )

    @Dao
    interface DeckDao {
        @Query("SELECT * FROM decks")
        fun getDecks(): List<Deck>

        @Query("SELECT * FROM decks WHERE id LIKE :deckId")
        fun getDeckById(deckId: Int)

        @Insert
        fun insert(deck: Deck)

        @Update
        fun update(deck: Deck)

        @Delete
        fun delete(deck: Deck)
    }

    @Entity(tableName = "cards", indices = [Index(value = ["deck_id"], unique = true)])
    data class Card(
        @PrimaryKey val id: Int,
        @ColumnInfo(name = "deck_id") val deckId: Int,
        @ColumnInfo(name = "card_prompt") val cardPrompt: String,
        @ColumnInfo(name = "card_text") val cardText: String
    )

    @Dao
    interface CardDao {
        @Query("SELECT * FROM cards WHERE deck_id LIKE :deckId")
        fun getCardsByDeck(deckId: Int)

        @Query("SELECT * FROM cards WHERE id LIKE :cardId")
        fun getCardById(cardId: Card)

        @Insert
        fun insert(card: Card)

        @Update
        fun update(card: Card)

        @Delete
        fun delete(card: Card)
    }

    @Database(entities = [Deck::class, Card::class], version = 1)
    abstract class AppDatabase : RoomDatabase() {
        abstract fun deckDao(): DeckDao
        abstract fun cardDao(): CardDao
    }
}