package com.quizlingo.quizlingo.businessrules

import com.quizlingo.quizlingo.businesslogic.*
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Test

class TestStudyDeck {
    @Test
    fun testStartStudyingCards() {
        val expectedDeck = Deck(1,
            listOf(
                Card(1, 1, "prompt: deck-1 card-1", "answer: deck-1 card-1"),
                Card(100000000000, 1, "prompt: deck-1 card-100000000000", "answer: deck-1 card-100000000000")
            ),
            2
        )

        val shower = object: DeckStudyShower {
            override fun showCardPrompt(card: Card) {
                Assert.assertEquals(expectedDeck.cards[0], card)
            }

            override fun showResponseCorrectness(correctness: ResponseCorrectness) {
                Assert.fail()
            }

            override fun showEndOfDeck() {
                Assert.fail()
            }
        }

        val studyDeck = StudyDeck(shower, expectedDeck)
        studyDeck.startStudyingCards()
    }

    @Test
    fun testAdvanceToNextCard() {
        val expectedDeck = Deck(1,
            listOf(
                Card(1, 1, "prompt: deck-1 card-1", "answer: deck-1 card-1"),
                Card(100000000000, 1, "prompt: deck-1 card-100000000000", "answer: deck-1 card-100000000000")
            ),
            2
        )
        Assert.assertEquals(expectedDeck.cardCount, expectedDeck.cards.size)
        var idx = 0
        val shower = object: DeckStudyShower {
            override fun showCardPrompt(card: Card) {
                Assert.assertTrue(idx < expectedDeck.cardCount)
                Assert.assertEquals(card, expectedDeck.cards[idx])
            }

            override fun showResponseCorrectness(correctness: ResponseCorrectness) {
                Assert.fail()
            }

            override fun showEndOfDeck() {
                Assert.assertTrue(idx == expectedDeck.cardCount)
            }
        }

        val studyDeck = StudyDeck(shower, expectedDeck)
        studyDeck.startStudyingCards()
        idx += 1
        studyDeck.advanceToNextCard()
        idx += 1
        studyDeck.advanceToNextCard()
    }

    @Test
    fun testScoreUserResponse() {
        val expectedDeck = Deck(1,
            listOf(
                Card(1, 1, "prompt: deck-1 card-1", "answer: deck-1 card-1"),
                Card(100000000000, 1, "prompt: deck-1 card-100000000000", "answer: deck-1 card-100000000000")
            ),
            2
        )
        Assert.assertEquals(expectedDeck.cardCount, expectedDeck.cards.size)

        var idx = 0

        lateinit var expectedResponseCorrectness: ResponseCorrectness

        val shower = object: DeckStudyShower {
            override fun showCardPrompt(card: Card) {
                Assert.assertTrue(idx < expectedDeck.cardCount)
                Assert.assertEquals(expectedDeck.cards[idx], card)
            }

            override fun showResponseCorrectness(correctness: ResponseCorrectness) {
                Assert.assertEquals(expectedResponseCorrectness, correctness)
            }

            override fun showEndOfDeck() {
                Assert.assertTrue(idx == expectedDeck.cardCount)
            }

        }

        val studyDeck = StudyDeck(shower, expectedDeck)
        studyDeck.startStudyingCards()

        var userResponse = UserResponse("a wrong response")
        expectedResponseCorrectness = ResponseCorrectness(false, userResponse, expectedDeck.cards[idx])
        studyDeck.scoreUserResponse(userResponse)

        userResponse = UserResponse("answer: deck-1 card-1")
        expectedResponseCorrectness = ResponseCorrectness(true, userResponse, expectedDeck.cards[idx])
        studyDeck.scoreUserResponse(userResponse)

        idx += 1
        studyDeck.advanceToNextCard()

        userResponse = UserResponse("answer: deck-1 card-100000000000")
        expectedResponseCorrectness = ResponseCorrectness(true, userResponse, expectedDeck.cards[idx])
        studyDeck.scoreUserResponse(userResponse)

        idx += 1
        studyDeck.advanceToNextCard()
    }
}