package com.quizlingo.quizlingo.businesslogic

data class Card(val id: Long, val deckId: Long, val prompt: String, val answer: String, val order: Int) {
    fun match(text : String) : Boolean {
        val parsedText = text.toLowerCase().trim().filter { it != ' '}
        val parsedAns = this.answer.toLowerCase().trim().filter { it != ' '}

        return (parsedText == parsedAns)
    }
}