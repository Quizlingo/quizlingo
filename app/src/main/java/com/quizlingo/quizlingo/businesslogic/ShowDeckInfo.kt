package com.quizlingo.quizlingo.businesslogic

class ShowDeckInfo(private val getter: DeckInfoGetter, private val shower: DeckInfoShower) {
    suspend fun showDeckInfo(id: Long) {
        shower.showDeckInfo(getter.getDeckInfo(id))
    }
}