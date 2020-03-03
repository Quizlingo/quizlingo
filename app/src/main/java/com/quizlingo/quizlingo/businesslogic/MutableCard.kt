package com.quizlingo.quizlingo.businesslogic

class MutableCard(card: Card) {
    var id: Long = card.id
    var deckId: Long = card.deckId
    var prompt: String = card.prompt
    var answer: String = card.answer

    fun toCard() = Card(id, deckId, prompt, answer)
}