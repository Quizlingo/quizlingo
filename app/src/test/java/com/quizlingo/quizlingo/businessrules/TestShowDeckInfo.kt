package com.quizlingo.quizlingo.businessrules

import com.quizlingo.quizlingo.businesslogic.*
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Test

class TestShowDeckInfo {
    @Test
    fun showDeckInfo() = runBlockingTest {
        val expectedDeck = Deck(100000000000,
            listOf(
                Card(20, 100000000000, "prompt: deck-100000000000 card-20", "answer: deck-100000000000 card-20")
            ),
            1
        )

        val getter = object: DeckInfoGetter {
            override suspend fun getDeckInfo(deckId: Long): Deck {
                Assert.assertEquals(expectedDeck.deckId, deckId)
                return expectedDeck
            }
        }

        val shower = object: DeckInfoShower {
            override fun showDeckInfo(deck: Deck) {
                Assert.assertEquals(expectedDeck, deck)
            }
        }

        val showDeckInfo = ShowDeckInfo(getter, shower)
        showDeckInfo.showDeckInfo(expectedDeck.deckId)
    }
}