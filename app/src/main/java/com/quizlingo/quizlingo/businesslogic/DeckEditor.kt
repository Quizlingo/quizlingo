package com.quizlingo.quizlingo.businesslogic

interface DeckEditor {
    fun showCurrentDeckInfo(deck: Deck)
    fun getUpdatedDeckInfo(): Deck
}