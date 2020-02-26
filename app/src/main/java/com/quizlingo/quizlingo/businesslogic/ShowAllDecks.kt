package com.quizlingo.quizlingo.businesslogic

class ShowAllDecks(private val getter: AllDecksGetter, private val shower: AllDecksShower) {
    suspend fun showAllDecks() {
        shower.showAllDecks(getter.getAllDecks())
    }
}