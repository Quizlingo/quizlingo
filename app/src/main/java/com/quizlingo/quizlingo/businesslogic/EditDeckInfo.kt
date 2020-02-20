package com.quizlingo.quizlingo.businesslogic

class EditDeckInfo(private val editor: DeckEditor, private val getter: DeckInfoGetter, private val saver: DeckSaver) {
    suspend fun showCurrentDeckInfo(id: Long) {
        editor.showCurrentDeckInfo(getter.getDeckInfo(id))
    }

    suspend fun saveUpdatedDeckInfo() {
        saver.saveDeck(editor.getUpdatedDeckInfo())
    }
}