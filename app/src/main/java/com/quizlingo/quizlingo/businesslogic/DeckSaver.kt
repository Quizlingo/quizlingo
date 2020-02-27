package com.quizlingo.quizlingo.businesslogic

interface DeckSaver {
    suspend fun saveDeck(deck: Deck): Deck
    suspend fun deleteDeck(deck: Deck)
}