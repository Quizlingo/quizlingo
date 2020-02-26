package com.quizlingo.quizlingo.businessrules

import com.quizlingo.quizlingo.businesslogic.*
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import org.junit.Assert

class TestShowAllDecks {

    @Test
    fun showAllDecks() = runBlockingTest {
        val expectedDecks = listOf(
            Deck(1,
                "Test Deck",
                "a deck solely for testing",
                listOf(
                    Card(1, 1, "prompt: deck-1 card-1", "answer: deck-1 card-1"),
                    Card(100000000000, 1, "prompt: deck-1 card-100000000000", "answer: deck-1 card-100000000000")),
                2
            ),
            Deck(100000000000,
                "A Different Test Deck",
                "A new deck also for testing",
                listOf(
                    Card(20, 100000000000, "prompt: deck-100000000000 card-20", "answer: deck-100000000000 card-20")
                ),
                1
            )
        )

        val getter = object: AllDecksGetter {
            override suspend fun getAllDecks(): List<Deck> {
                return expectedDecks
            }
        }
        val shower = object: AllDecksShower {
            override fun showAllDecks(decks: List<Deck>) {
                Assert.assertEquals(expectedDecks, decks)
            }
        }

        val showAllDecks = ShowAllDecks(getter, shower)

        showAllDecks.showAllDecks()
    }
}