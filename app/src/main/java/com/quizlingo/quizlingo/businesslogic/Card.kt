package com.quizlingo.quizlingo.businesslogic

data class Card(val id: Long, val deckId: Long, val prompt: String, val answer: String, val order: Int)