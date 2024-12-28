package it.polito.BeeDone.task.history

import it.polito.BeeDone.profile.User

class Event(
    var title: String,
    var date: String,
    var taskStatus: String,
    var user: String,
    var taskDoneSubtasks: String,
    var taskTotalSubtasks: String,
    var taskChanges: List<String>
) {
    constructor() : this("", "", "Pending", "", "", "", mutableListOf())
}