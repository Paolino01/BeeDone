package it.polito.BeeDone.utils.questions_answers

import it.polito.BeeDone.profile.User

class Question (
    var questionId: String,
    var text: String,
    var date: String,
    var user: String,
    var answers: MutableList<String>
) {
    constructor() : this("", "", "", "", mutableListOf())
}