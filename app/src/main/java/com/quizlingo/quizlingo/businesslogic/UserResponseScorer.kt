package com.quizlingo.quizlingo.businesslogic

class UserResponseScorer {
    fun scoreUserResponse(userResponse: UserResponse, card: Card): ResponseCorrectness {
        return ResponseCorrectness(userResponse.response == card.answer, userResponse, card)
    }
}