package com.quizlingo.verticalprototype

import android.content.Context
import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.room.*

class SaveDeckTask(private val dao: DatabaseComponent.DeckDao, private val then: ((Long) -> Unit)?) : AsyncTask<DatabaseComponent.Deck, Void, Long>() {
    override fun doInBackground(vararg params: DatabaseComponent.Deck?): Long {
        return if(params[0]!!.id == 0L) {
            dao.insert(params[0]!!)
        } else {
            dao.update(params[0]!!)
            params[0]!!.id
        }
    }

    override fun onPostExecute(result: Long?) {
        then?.invoke(result!!)
    }
}

class SaveCardsTask(private val dao: DatabaseComponent.CardDao) : AsyncTask<DatabaseComponent.Card, Void, Unit>() {
    override fun doInBackground(vararg params: DatabaseComponent.Card?) {
        params.forEach {
            if(it != null) {
                if(it.id == 0L) {
                    dao.insert(it)
                } else {
                    dao.update(it)
                }
            }
        }
    }
}

class DatabaseComponent(val context: Context) {

    @Entity(tableName = "decks")
    data class Deck(
        @PrimaryKey(autoGenerate = true) val id: Long,
        @ColumnInfo(name = "deck_name") var deckName: String,
        @ColumnInfo(name = "deck_description") var deckDescription: String,
        @ColumnInfo(name = "deck_card_count") var deckCardCount: Int
    )

    @Dao
    interface DeckDao {
        @Query("SELECT * FROM decks ORDER BY id ASC")
        fun getDecks(): LiveData<List<Deck>>

        @Query("SELECT * FROM decks WHERE id LIKE :deckId")
        fun getDeckById(deckId: Long): LiveData<Deck>

        @Query("SELECT * FROM decks ORDER BY id ASC LIMIT 1 OFFSET :n")
        fun getDeckByPosition(n: Int): LiveData<Deck>

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun insert(deck: Deck): Long

        @Update
        fun update(deck: Deck)

        @Delete
        fun delete(deck: Deck)
    }

    @Entity(tableName = "cards")
    data class Card(
        @PrimaryKey(autoGenerate = true) val id: Long,
        @ColumnInfo(name = "deck_id") var deckId: Long,
        @ColumnInfo(name = "card_prompt") var cardPrompt: String,
        @ColumnInfo(name = "card_text") var cardText: String
    )

    @Dao
    interface CardDao {
        @Query("SELECT * FROM cards WHERE deck_id LIKE :deckId ORDER BY id ASC")
        fun getCardsByDeck(deckId: Long): LiveData<List<Card>>

        @Query("SELECT * FROM cards WHERE id LIKE :cardId")
        fun getCardById(cardId: Long): LiveData<Card>

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun insert(card: Card): Long

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun insertMany(cards: List<Card>)

        @Update
        fun update(card: Card)

        @Update
        fun updateMany(cards: List<Card>)

        @Delete
        fun delete(card: Card)
    }

    @Database(entities = [Deck::class, Card::class], version = 1)
    abstract class AppDatabase : RoomDatabase() {
        abstract fun deckDao(): DeckDao
        abstract fun cardDao(): CardDao
    }


    companion object {
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = instance
            if(tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val tempInstance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "cards.db").build()
                instance = tempInstance
                return tempInstance
            }

        }
    }

}