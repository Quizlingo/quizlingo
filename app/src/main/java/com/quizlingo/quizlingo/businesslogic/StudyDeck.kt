package com.quizlingo.quizlingo.businesslogic

class StudyDeck(private val shower: DeckStudyShower, private val deck: Deck) {
    private val scorer = UserResponseScorer()

    private var cardIdx: Int = 0

    fun startStudyingCards() {
        cardIdx = 0
        shower.showCardPrompt(deck.cards[cardIdx])
    }

    fun scoreUserResponse(userResponse: UserResponse) {
        val correctness = scorer.scoreUserResponse(userResponse, deck.cards[cardIdx])
        shower.showResponseCorrectness(correctness)
    }

    fun advanceToNextCard() {
        cardIdx += 1
        if(cardIdx >= deck.cards.size) {
            shower.showEndOfDeck()
        } else {
            shower.showCardPrompt(deck.cards[cardIdx])
        }
    }
}