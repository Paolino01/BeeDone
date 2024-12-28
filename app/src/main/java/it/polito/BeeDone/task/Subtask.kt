package it.polito.BeeDone.task

class Subtask(
    var subtaskId: String,
    var subtaskTitle: String,
    var subtaskState: String
) {
    constructor() : this("", "", "Not Completed")
}

fun setState(sub: Subtask?) {
    sub?.subtaskState = "Completed"
}