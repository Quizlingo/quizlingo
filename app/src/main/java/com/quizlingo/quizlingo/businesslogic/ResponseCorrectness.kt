package com.quizlingo.quizlingo.businesslogic

data class ResponseCorrectness(val isCorrect: Boolean, val userResponse: UserResponse, val card: Card)