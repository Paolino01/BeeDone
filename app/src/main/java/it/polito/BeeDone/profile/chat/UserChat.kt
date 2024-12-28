package it.polito.BeeDone.profile.chat

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import it.polito.BeeDone.profile.User


/**
 * Represents a chat between two users
 * @param user1: the first user involved in the chat
 * @param user2: the other user involved in the chat
 * @param messages: list of messages sent between the two users
 */
class UserChat (
    var user1: String,
    var user2: String,
    var messages: SnapshotStateList<Message>
) {
    constructor() : this("", "", mutableStateListOf())
}