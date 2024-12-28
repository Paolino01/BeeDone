package it.polito.BeeDone.profile.chat

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.BeeDone.profile.User
import it.polito.BeeDone.profile.loggedUser
import it.polito.BeeDone.utils.CreateImage
import it.polito.BeeDone.utils.CreateTextFieldNoError
import it.polito.BeeDone.utils.lightBlue
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date

@SuppressLint("UnrememberedMutableState", "SimpleDateFormat")
@Composable
fun UserChatPane(
    selectedUser: User,
    userMessageValue: String,
    setUserMessage: (String) -> Unit,
    showUserInformationPane: (String) -> Unit,
    db: FirebaseFirestore
) {
    var chat by remember { mutableStateOf(UserChat()) }
    var userChats by remember { mutableStateOf<List<UserChat>>(emptyList()) }

    LaunchedEffect(loggedUser.userChat) {
        val chats = loggedUser.userChat.mapNotNull { userChatRef ->
            val doc = db.collection("UserChats").document(userChatRef).get().await()
            if (doc.exists()) {
                val messageRefs = doc.get("messages") as? List<String> ?: listOf()
                val messages = messageRefs.mapNotNull { ref ->
                    val messageDoc = db.collection("Messages").document(ref).get().await()
                    messageDoc.toObject(Message::class.java)
                }.toMutableStateList()
                UserChat(doc.getString("user1")!!, doc.getString("user2")!!, messages)
            } else null
        }
        userChats = chats

        val selectedChat = userChats.find {
            it.user1 == selectedUser.userNickname || it.user2 == selectedUser.userNickname
        }
        if (selectedChat == null) {
            val newChat = UserChat(loggedUser.userNickname, selectedUser.userNickname, mutableStateListOf())
            chat = newChat
            db.collection("UserChats").add(newChat)
                .addOnSuccessListener { documentReference ->
                    val chatId = documentReference.id

                    loggedUser.userChat.add(chatId)

                    // Update the userChat arrays in both user documents
                    db.collection("Users").document(loggedUser.userNickname)
                        .update("userChat", FieldValue.arrayUnion(chatId))
                        .addOnSuccessListener {
                            Log.d("Firestore", "Logged user userChat updated with ID: $chatId")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error updating logged user userChat", e)
                        }

                    db.collection("Users").document(selectedUser.userNickname)
                        .update("userChat", FieldValue.arrayUnion(chatId))
                        .addOnSuccessListener {
                            Log.d("Firestore", "Selected user userChat updated with ID: $chatId")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error updating selected user userChat", e)
                        }
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error adding document", e)
                }
        } else {
            chat = selectedChat
        }
    }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            BottomBar(userMessageValue, setUserMessage, chat, db, userChats)
        }
    ) { innerPadding ->
        LazyColumn(
            Modifier
                .padding(innerPadding)
                .padding(top = 16.dp)
        ) {
            items(chat.messages) { message ->
                ChatMessage(
                    message = message,
                    selectedUser = selectedUser,
                    loggedUser = loggedUser,
                    screenWidth = screenWidth,
                    showUserInformationPane = showUserInformationPane,
                    context = context
                )
            }
        }
    }
}

@Composable
fun BottomBar(
    userMessageValue: String,
    setUserMessage: (String) -> Unit,
    chat: UserChat,
    db: FirebaseFirestore,
    userChats: List<UserChat>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(250, 250, 250))
            .padding(bottom = 7.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CreateTextFieldNoError(
            value = userMessageValue,
            setValue = setUserMessage,
            label = "Write your message here.",
            keyboardType = KeyboardType.Text,
            modifier = Modifier.fillMaxWidth(0.85f),
            maxLines = 3
        )
        IconButton(onClick = {
            if (userMessageValue.isNotBlank()) {
                val newMessage = Message(
                    userMessageValue,
                    SimpleDateFormat("dd/MM/yyyy").format(Date()),
                    SimpleDateFormat("hh:mm").format(Date()),
                    loggedUser.userNickname
                )

                // Add message to the "Messages" collection
                db.collection("Messages").add(newMessage)
                    .addOnSuccessListener { messageDocumentReference ->
                        val messageId = messageDocumentReference.id
                        chat.messages.add(newMessage)

                        // Update chat in the database
                        val chatIndex = userChats.indexOf(chat)
                        if (chatIndex >= 0) {
                            db.collection("UserChats")
                                .document(loggedUser.userChat[chatIndex])
                                .update("messages", FieldValue.arrayUnion(messageId))
                                .addOnSuccessListener {
                                    Log.d("Firestore", "Message successfully added to chat")
                                }
                                .addOnFailureListener { e ->
                                    Log.w("Firestore", "Error adding message to chat", e)
                                }
                        }
                        setUserMessage("") // Reset the message
                    }
                    .addOnFailureListener { e ->
                        Log.w("Firestore", "Error adding message document", e)
                    }
            }
        }) {
            Icon(Icons.AutoMirrored.Filled.Send, "Send")
        }
    }
}

@Composable
fun ChatMessage(
    message: Message,
    selectedUser: User,
    loggedUser: User,
    screenWidth: Int,
    showUserInformationPane: (String) -> Unit,
    context: Context
) {
    val linkPattern = Regex("(https?://[\\w-]+(\\.[\\w-]+)+([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?)")

    val annotatedString = buildAnnotatedString {
        var lastIndex = 0
        val matches = linkPattern.findAll(message.message)

        for (match in matches) {
            append(message.message.substring(startIndex = lastIndex, endIndex = match.range.first))
            pushStringAnnotation(tag = "URL", annotation = match.value)
            withStyle(style = SpanStyle(color = lightBlue, textDecoration = TextDecoration.Underline)) {
                append(match.value)
            }
            pop()
            lastIndex = match.range.last + 1
        }
        append(message.message.substring(lastIndex))
    }

    if (message.sender == loggedUser.userNickname) {
        // Sent message: align it to the right
        SentMessage(message, annotatedString, loggedUser, screenWidth, showUserInformationPane, context)
    } else {
        // Received message: align it to the left
        ReceivedMessage(message, annotatedString, selectedUser, screenWidth, showUserInformationPane, context)
    }
}

@Composable
fun SentMessage(
    message: Message,
    annotatedString: AnnotatedString,
    loggedUser: User,
    screenWidth: Int,
    showUserInformationPane: (String) -> Unit,
    context: Context
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Column(
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .widthIn(0.dp, (screenWidth * 0.7).dp)
                .border(
                    1.dp, Color.Gray, RoundedCornerShape(
                        topStart = 47f,
                        topEnd = 0f,
                        bottomStart = 47f,
                        bottomEnd = 47f
                    )
                )
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            ClickableText(
                text = annotatedString,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp, textAlign = TextAlign.Left),
                onClick = { offset ->
                    annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                        .firstOrNull()?.let { annotation ->
                            val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                            context.startActivity(urlIntent)
                        }
                }
            )
            Text(
                text = "${message.date} - ${message.time}",
                modifier = Modifier
                    .padding(top = 3.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.End,
                fontSize = 15.sp,
                color = Color.DarkGray
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier
                .padding(end = 10.dp)
                .clickable { showUserInformationPane(loggedUser.userNickname) }
        ) {
            CreateImage(
                photo = loggedUser.userImage?.toUri(),
                name = "${loggedUser.userFirstName} ${loggedUser.userLastName}",
                size = 30
            )
        }
    }
    Spacer(modifier = Modifier.height(5.dp))
}

@Composable
fun ReceivedMessage(
    message: Message,
    annotatedString: AnnotatedString,
    selectedUser: User,
    screenWidth: Int,
    showUserInformationPane: (String) -> Unit,
    context: Context
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier.clickable { showUserInformationPane(selectedUser.userNickname) },
            horizontalAlignment = Alignment.End
        ) {
            CreateImage(
                photo = selectedUser.userImage?.toUri(),
                name = "${selectedUser.userFirstName} ${selectedUser.userLastName}",
                size = 30
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .widthIn(0.dp, (screenWidth * 0.7).dp)
                .background(
                    Color(230, 230, 230), RoundedCornerShape(
                        topStart = 0f,
                        topEnd = 47f,
                        bottomStart = 47f,
                        bottomEnd = 47f
                    )
                )
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            ClickableText(
                text = annotatedString,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp, textAlign = TextAlign.Left),
                onClick = { offset ->
                    annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                        .firstOrNull()?.let { annotation ->
                            val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                            context.startActivity(urlIntent)
                        }
                }
            )
            Text(
                text = "${message.date} - ${message.time}",
                modifier = Modifier
                    .padding(top = 3.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.End,
                fontSize = 15.sp,
                color = Color.DarkGray
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
    Spacer(modifier = Modifier.height(5.dp))
}
