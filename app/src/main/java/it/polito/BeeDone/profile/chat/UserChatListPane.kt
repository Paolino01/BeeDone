package it.polito.BeeDone.profile.chat

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.BeeDone.profile.User
import it.polito.BeeDone.profile.loggedUser
import it.polito.BeeDone.utils.CreateImage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun UserChatListPane(
    userChat: MutableList<String>,
    userChatPane: (String) -> Unit,
    db: FirebaseFirestore
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp

    // variables for the flyout button
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var showButton by remember { mutableStateOf(false) }
    var buttonText by remember { mutableStateOf("↑") }

    LaunchedEffect(listState.firstVisibleItemIndex, listState.layoutInfo.visibleItemsInfo) {
        showButton = listState.firstVisibleItemIndex > 0
        buttonText = if (listState.firstVisibleItemIndex + listState.layoutInfo.visibleItemsInfo.size >= listState.layoutInfo.totalItemsCount) "↑" else "↓"
    }

    if (userChat.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "No chat to display")
        }
    } else {
        LazyColumn(state = listState) {
            items(userChat) { c ->

                var chat by remember { mutableStateOf(UserChat()) }
                var receiver by remember { mutableStateOf(User()) }
                var isLoading by remember { mutableStateOf(true) }

                LaunchedEffect(c) {
                    coroutineScope.launch {
                        try {
                            val chatDoc = db.collection("UserChats").document(c).get().await()
                            if (chatDoc.exists()) {
                                chat.user1 = chatDoc.getString("user1") ?: ""
                                chat.user2 = chatDoc.getString("user2") ?: ""

                                val userIdToFetch = if (chat.user1 == loggedUser.userNickname) chat.user2 else chat.user1
                                val userDoc = db.collection("Users").document(userIdToFetch).get().await()
                                receiver = userDoc.toObject(User::class.java) ?: User()

                                chat.messages.clear()
                                val messageRefs = chatDoc.get("messages") as? List<String> ?: listOf()
                                messageRefs.forEach { ref ->
                                    val messageDoc = db.collection("Messages").document(ref).get().await()
                                    messageDoc.toObject(Message::class.java)?.let { chat.messages.add(it) }
                                }
                            }
                            isLoading = false
                        } catch (e: Exception) {
                            Log.e("Firestore", "Error getting user chat", e)
                            isLoading = false
                        }
                    }
                }

                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Row(
                        modifier = Modifier
                            .clickable { userChatPane(receiver.userNickname) }
                            .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CreateImage(
                                photo = receiver.userImage?.toUri(),
                                name = "${receiver.userFirstName} ${receiver.userLastName}",
                                size = if (screenWidth < screenHeight) 50 else 60
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier
                                .weight(if (screenWidth < screenHeight) 6f else 10f)
                        ) {
                            Text(
                                text = receiver.userNickname,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )

                            if (chat.messages.isNotEmpty()) {
                                val lastMessage = chat.messages.last()
                                Text(
                                    text = "${lastMessage.date} - ${lastMessage.time}",
                                    color = Color.Gray
                                )

                                Text(
                                    text = (if (loggedUser.userNickname == lastMessage.sender) "You: " else "") + lastMessage.message,
                                    fontSize = 18.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = Color.LightGray)
                }
            }
        }

        // Scroll to top or bottom button
        if (showButton) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        if (listState.firstVisibleItemIndex + listState.layoutInfo.visibleItemsInfo.size < listState.layoutInfo.totalItemsCount) {
                            listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
                        } else {
                            listState.animateScrollToItem(0)
                        }
                    }
                },
                shape = CircleShape,
                modifier = Modifier
                    .padding(16.dp)
                    .offset(10.dp, 0.dp)
            ) {
                Text(buttonText, fontSize = 25.sp)
            }
        }
    }
}
