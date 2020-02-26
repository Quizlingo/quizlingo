package com.quizlingo.quizlingo.businesslogic

interface DeckInfoGetter {
    suspend fun getDeckInfo(deckId: Long): Deck
}