package it.polito.BeeDone.profile.chat

import it.polito.BeeDone.profile.User
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Contains the information of a message sent between two users
 * @param message: the text of the message
 * @param date
 * @param time
 * @param sender: the profile of the user who sent the message
 */
class Message(
    var message: String,
    var date: String,
    var time: String,
    var sender: String
) {
    constructor() : this("", SimpleDateFormat("dd/MM/yyyy").format(Date()), SimpleDateFormat("hh:mm").format(Date()), "")
}