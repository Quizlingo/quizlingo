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

    private var allDecksGetter: AllDecksGetter
    private var singleDeckSaver: DeckSaver

    private fun isValidDeck(deck: Deck) = deck.title.isNotBlank()

    init {
        val db = PersistenceDatabase(application)
        allDecksGetter = db
        singleDeckSaver = db

        currentDeck.observeForever { deck: Deck ->
            if(isValidDeck(deck)) {
                if (allDecks.value!!.find { it.deckId == deck.deckId } != null) {
                    // Update existing deck
                    allDecks.value =
                        allDecks.value!!.map { if (it.deckId == deck.deckId) deck else it }
                    viewModelScope.launch {
                        singleDeckSaver.saveDeck(deck)
                    }
                } else {
                    // Add a new deck
                    viewModelScope.launch {
                        val deckWithId = singleDeckSaver.saveDeck(deck)
                        allDecks.value = allDecks.value!! + deckWithId
                    }
                }
            }

        }

        viewModelScope.launch {
            allDecks.value = allDecksGetter.getAllDecks()
        }
    }

    fun deleteDeck(deck: Deck) {
        viewModelScope.launch{
            allDecks.value = allDecks.value!! - deck
            singleDeckSaver.deleteDeck(deck)
        }
    }


}