package com.quizlingo.quizlingo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.quizlingo.quizlingo.businesslogic.*
import com.quizlingo.quizlingo.persistence.PersistenceDatabase
import kotlinx.coroutines.launch

class MainViewModel(application: Application): AndroidViewModel(application) {
    val allDecks: MutableLiveData<List<Deck>> = MutableLiveData()
    val currentDeck: MutableLiveData<Deck> = MutableLiveData()

    private lateinit var allDecksGetter: AllDecksGetter
    private lateinit var singleDeckSaver: DeckSaver

    init {
        val db = PersistenceDatabase(application)
        allDecksGetter = db
        singleDeckSaver = db

        currentDeck.observeForever{deck: Deck ->
            if(! allDecks.value!!.contains(deck)) {
                allDecks.value = allDecks.value!! + deck
            }
        }

        viewModelScope.launch {
            allDecks.value = allDecksGetter.getAllDecks()
        }
    }


}