package com.quizlingo.quizlingo.businesslogic

interface AllDecksGetter {
    suspend fun getAllDecks(): List<Deck>
}