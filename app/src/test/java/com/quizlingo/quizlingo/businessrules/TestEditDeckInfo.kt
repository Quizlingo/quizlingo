package com.quizlingo.quizlingo.businessrules

import com.quizlingo.quizlingo.businesslogic.*
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Test

class TestEditDeckInfo {
    @Test
    fun testShowCurrentDeckInfo() = runBlockingTest {
        val preEditDeck = Deck(1,
            "Test Deck",
            "a deck solely for testing",
            listOf(
                Card(1, 1, "prompt: deck-1 card-1", "answer: deck-1 card-1"),
                Card(100000000000, 1, "prompt: deck-1 card-100000000000", "answer: deck-1 card-100000000000")
            ),
            2
        )
        Assert.assertEquals(preEditDeck.cardCount, preEditDeck.cards.size)
        val postEditDeck = Deck(1,
            "A Different Test Deck",
            "A new deck also for testing",
            listOf(
                Card(1, 1, "different prompt: deck-1 card-1", "different answer: deck-1 card-1")
            ),
            1
        )
        Assert.assertEquals(postEditDeck.cardCount, postEditDeck.cards.size)
        val editor = object: DeckEditor {
            override fun showCurrentDeckInfo(deck: Deck) {
                Assert.assertEquals(preEditDeck, deck)
            }

            override fun getUpdatedDeckInfo(): Deck {
                return postEditDeck
            }

        }

        val getter = object: DeckInfoGetter {
            override suspend fun getDeckInfo(deckId: Long): Deck {
                Assert.assertEquals(preEditDeck.deckId, deckId)
                return preEditDeck
            }
        }

        val saver = object: DeckSaver {
            override suspend fun saveDeck(deck: Deck) {
                Assert.assertEquals(postEditDeck, deck)
            }
        }

        val editDeckInfo = EditDeckInfo(editor, getter, saver)

        editDeckInfo.showCurrentDeckInfo(preEditDeck.deckId)
        editDeckInfo.saveUpdatedDeckInfo()
    }
}