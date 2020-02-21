package com.quizlingo.quizlingo.businesslogic

data class Deck(
    val deckId: Long,
    val title: String,
    val description: String,
    val cards: List<Card>,
    val cardCount: Int
)