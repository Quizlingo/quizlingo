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
            if (!allDecks.value!!.contains(deck)) {
                allDecks.value = allDecks.value!! + deck
            }
        }

        viewModelScope.launch {
            // Bogus data for testing decks
            var decks = ArrayList<Deck>()
            decks.add(0, Deck(0, "asdf", "", Collections.emptyList(), 1))
            decks.add(0, Deck(0, "deck2", "", Collections.emptyList(), 1))
            decks.add(0, Deck(0, "deck3", "", Collections.emptyList(), 1))
            decks.add(0, Deck(0, "deck4", "", Collections.emptyList(), 1))
            allDecks.value = decks
            // End of bogus data

//            allDecks.value = allDecksGetter.getAllDecks()
        }
    }


}