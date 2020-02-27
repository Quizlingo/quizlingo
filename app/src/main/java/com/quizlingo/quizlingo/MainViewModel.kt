package com.quizlingo.quizlingo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.quizlingo.quizlingo.businesslogic.*
import com.quizlingo.quizlingo.persistence.PersistenceDatabase
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val allDecks: MutableLiveData<List<Deck>> = MutableLiveData()
    val currentDeck: MutableLiveData<Deck> = MutableLiveData()

    private lateinit var allDecksGetter: AllDecksGetter
    private lateinit var singleDeckSaver: DeckSaver

    init {
        val db = PersistenceDatabase(application)
        allDecksGetter = db
        singleDeckSaver = db

        currentDeck.observeForever { deck: Deck ->
            if(allDecks.value!!.find{it.deckId == deck.deckId} != null) {
                // Update existing deck
                allDecks.value = allDecks.value!!.map{ if(it.deckId == deck.deckId) deck else it }
            } else {
                // Add a new deck
                allDecks.value = allDecks.value!! + deck
            }

        }

        viewModelScope.launch {
            // Bogus data for testing decks
            var decks = ArrayList<Deck>()
            decks.add(
                Deck(
                    1, "Animals", "tbd",
                    cards = listOf(
                        Card(1, 1, "small animal", "cat"),
                        Card(2, 1, "flying dairy object", "butterfly"),
                        Card(3, 1, "angry cat", "tiger")
                    )
                )
            )
            decks.add(
                Deck(
                    2, "Animals2", "tbd",
                    cards = listOf(
                        Card(1, 2, "small animal", "cat"),
                        Card(2, 2, "flying dairy object", "butterfly"),
                        Card(3, 2, "angry cat", "tiger")
                    )
                )
            )
            decks.add(
                Deck(
                    3, "Animals3", "tbd",
                    cards = listOf(
                        Card(1, 3, "small animal", "cat"),
                        Card(2, 3, "flying dairy object", "butterfly"),
                        Card(3, 3, "angry cat", "tiger")
                    )
                )
            )
            decks.add(
                Deck(
                    4, "Animals4", "tbd",
                    cards = listOf(
                        Card(1, 4, "small animal", "cat"),
                        Card(2, 4, "flying dairy object", "butterfly"),
                        Card(3, 4, "angry cat", "tiger")
                    )
                )
            )
            allDecks.value = decks
            // End of bogus data

//            allDecks.value = allDecksGetter.getAllDecks()
        }
    }


}