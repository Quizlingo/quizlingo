package com.quizlingo.verticalprototype

import android.content.Context
import android.os.AsyncTask
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import java.lang.ref.WeakReference

class LoadDecksTask(inDatabase: DatabaseComponent.AppDatabase, outLiveData: MutableLiveData<List<DatabaseComponent.Deck>>) :
    AsyncTask<Void, Void, List<DatabaseComponent.Deck>?>() {

    private val db = WeakReference<DatabaseComponent.AppDatabase>(inDatabase)
    private val outLiveData = WeakReference<MutableLiveData<List<DatabaseComponent.Deck>>>(outLiveData)

    override fun doInBackground(vararg params: Void?): List<DatabaseComponent.Deck>? {
        return db.get()?.deckDao()?.getDecks()
    }

    override fun onPostExecute(result: List<DatabaseComponent.Deck>?) {
        outLiveData.get()?.value = result
    }

}

class DatabaseComponent(val context: Context) {

    @Entity(tableName = "decks")
    data class Deck(
        @PrimaryKey(autoGenerate = true) val id: Int,
        @ColumnInfo(name = "deck_name") val deckName: String,
        @ColumnInfo(name = "deck_description") val deckDescription: String,
        @ColumnInfo(name = "deck_card_count") val deckCardCount: Int
    )

    @Dao
    interface DeckDao {
        @Query("SELECT * FROM decks ORDER BY id ASC")
        fun getDecks(): List<Deck>

        @Query("SELECT * FROM decks WHERE id LIKE :deckId")
        fun getDeckById(deckId: Int): Deck

        @Query("SELECT * FROM decks ORDER BY id ASC LIMIT 1 OFFSET :n")
        fun getDeckByPosition(n: Int): Deck

        @Insert
        fun insert(deck: Deck)

        @Update
        fun update(deck: Deck)

        @Delete
        fun delete(deck: Deck)
    }

    @Entity(tableName = "cards", indices = [Index(value = ["deck_id"], unique = true)])
    data class Card(
        @PrimaryKey(autoGenerate = true) val id: Int,
        @ColumnInfo(name = "deck_id") val deckId: Int,
        @ColumnInfo(name = "card_prompt") val cardPrompt: String,
        @ColumnInfo(name = "card_text") val cardText: String
    )

    @Dao
    interface CardDao {
        @Query("SELECT * FROM cards WHERE deck_id LIKE :deckId ORDER BY id ASC")
        fun getCardsByDeck(deckId: Int): List<Card>

        @Query("SELECT * FROM cards WHERE id LIKE :cardId")
        fun getCardById(cardId: Int): Card

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


    companion object {
        val databases: MutableList<Pair<Context, AppDatabase>> = mutableListOf()
    }

    // Don't you just love kotlin's ability to take some simple logic and make it impossible to read?
    // This checks if databases contains a database for the current context, and creates a new one if necessary
    fun getDatabase() : AppDatabase = databases.find{it.first == context}?.second ?: Room.databaseBuilder(context, AppDatabase::class.java, "cardsDB").build().also { databases.add(Pair(context, it)) }
}