package com.quizlingo.quizlingo.businesslogic

class MutableDeck(deck: Deck) {
    var deckId: Long = deck.deckId
    var title: String = deck.title
    var description: String = deck.description
    val cards: MutableList<MutableCard> = deck.cards.map{ MutableCard(it) }.toMutableList()
    val cardCount: Int
        get() = cards.size

    fun toDeck() = Deck(deckId, title, description, cards.map{it.toCard()})
}