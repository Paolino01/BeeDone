package it.polito.BeeDone.utils.questions_answers

import it.polito.BeeDone.profile.User

class Answer (
    var text: String,
    var date: String,
    var user: String
) {
    constructor() : this("", "", "")
}