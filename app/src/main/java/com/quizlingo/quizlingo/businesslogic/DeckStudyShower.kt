package com.quizlingo.quizlingo.businesslogic

interface DeckStudyShower {
    fun showCardPrompt(card: Card)
    fun showResponseCorrectness(correctness: ResponseCorrectness)
    fun showEndOfDeck()
}