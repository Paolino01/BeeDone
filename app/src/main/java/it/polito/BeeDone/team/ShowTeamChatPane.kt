package it.polito.BeeDone.team

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
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
import it.polito.BeeDone.profile.chat.Message
import it.polito.BeeDone.profile.chat.UserChat
import it.polito.BeeDone.profile.loggedUser
import it.polito.BeeDone.utils.CreateImage
import it.polito.BeeDone.utils.CreateTextFieldNoError
import it.polito.BeeDone.utils.lightBlue
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun ShowTeamChatPane(
    selectedTeam: Team,
    chat: MutableList<Message>,
    setChat: (String, Team) -> Unit,
    messageValue: String,
    setMessage: (String) -> Unit,
    showUserInformationPane: (String) -> Unit,
    db: FirebaseFirestore
) {

    //val allUser = mutableListOf<TeamMember>()
    var allUser = remember { mutableStateListOf<TeamMember>() }
    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(allUser) {
        for (tk in selectedTeam.teamMembers) {
            val d = db.collection("TeamMembers").document(tk).get().await()
            if(d.exists()) {
                    allUser.add(d.toObject(TeamMember::class.java)!!)
                    isLoading = false
                }
        }
    }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    val context = LocalContext.current

    //variables for the flyout button
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var showButton by remember { mutableStateOf(false) }
    var buttonText by remember { mutableStateOf("↑") }

    LaunchedEffect(listState.firstVisibleItemIndex, listState.layoutInfo.visibleItemsInfo) {
        showButton = listState.firstVisibleItemIndex > 0
        buttonText = if (listState.firstVisibleItemIndex + listState.layoutInfo.visibleItemsInfo.size >= listState.layoutInfo.totalItemsCount) "↑" else "↓"
    }

    if(!isLoading) {
        Scaffold(
            bottomBar = {
                Column(
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Row(
                        modifier = Modifier
                            .background(Color(250, 250, 250))
                            .fillMaxWidth()
                    ) {       //When we want to tag another user with @, we show a list of the team users to select from
                        if (messageValue.isNotEmpty() && messageValue.last() == '@') {
                            ShowTeamUserList(
                                selectedTeam = selectedTeam,
                                messageValue = messageValue,
                                setMessage = setMessage,
                                db = db
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .background(
                                Color(
                                    250, 250, 250
                                )
                            )   //Same color as the background
                            .fillMaxWidth()
                            .padding(bottom = 7.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CreateTextFieldNoError(
                            value = messageValue,
                            setValue = setMessage,
                            label = "Write your message here.",
                            keyboardType = KeyboardType.Text,
                            modifier = Modifier.fillMaxWidth(0.85f),
                            maxLines = 3
                        )
                        IconButton(onClick = {
                            if (messageValue.isNotBlank()) {            //If the message is blank, nothing happens

                                //Create the message object
                                val message = Message(
                                    message = messageValue,
                                    date = SimpleDateFormat("dd/MM/yyyy").format(Date()),
                                    time = SimpleDateFormat("hh:mm").format(Date()),
                                    sender = loggedUser.userNickname
                                )

                                // Add the message to the Message collection
                                db.collection("Messages")
                                    .add(message).addOnSuccessListener { doc ->
                                        selectedTeam.teamChat.add(doc.id)
                                        // Add the reference to the teamChat field in the Team collection
                                        db.collection("Team")
                                            .document(selectedTeam.teamId)
                                            .update("teamChat", selectedTeam.teamChat)

                                        //Update the messageToRead status for all team members
                                        selectedTeam.teamMembers.forEach { memberId ->
                                            if (memberId != loggedUser.userNickname) {
                                                db.collection("UserInTeam")
                                                    .whereEqualTo("first", selectedTeam.teamId)
                                                    .get()
                                                    .addOnSuccessListener { documents ->
                                                        for (document in documents) {
                                                            // Update the second field to true (message unread)
                                                            document.reference.update(
                                                                "second",
                                                                true
                                                            )
                                                        }
                                                    }
                                            }
                                        }
                                    }
                                setMessage("")
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.Send, "Send")
                        }
                    }
                }
            }
        ) { innerPadding ->
            /*for (i in 0..<loggedUser.userTeams.size) {           //When the user enters in the TeamChat, set messages as read
            if (loggedUser.userTeams[i] == selectedTeam.teamId) {
                loggedUser.userTeams[i] = Pair(selectedTeam, false)
                break
            }
        }*/

            // When the user enters the team chat, mark messages as read
            db.collection("UserInTeam")
                .whereEqualTo("first", selectedTeam.teamId)
                .get().addOnSuccessListener { documents ->
                    for (document in documents) {
                        document.reference.update("second", false)
                    }
                }

            LazyColumn(
                Modifier
                    .padding(innerPadding)
                    .padding(top = 10.dp),
                state = listState
            ) {

                items(chat) { m ->
                    val linkPattern =
                        Regex("(https?://[\\w-]+(\\.[\\w-]+)+([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?)")
                    val userTagPattern = Regex("@\\S+")

                    val annotatedString = buildAnnotatedString {
                        var lastIndex = 0
                        val matches =
                            (linkPattern.findAll(m.message) + userTagPattern.findAll(m.message)).sortedBy { it.range.first }

                        for (match in matches) {
                            append(
                                m.message.substring(
                                    startIndex = lastIndex,
                                    endIndex = match.range.first
                                )
                            )
                            val annotationTag =
                                if (match.value.startsWith("@")) "USER_TAG" else "URL"
                            val nicknames = allUser.map { it.user }.map { it }
                            val isExistingNickname =
                                match.value.startsWith("@") && nicknames.contains(
                                    match.value.substring(0)
                                )

                            if (isExistingNickname || annotationTag == "URL") {
                                pushStringAnnotation(tag = annotationTag, annotation = match.value)
                                withStyle(
                                    style = SpanStyle(
                                        color = lightBlue,
                                        textDecoration = TextDecoration.Underline
                                    )
                                ) {
                                    append(match.value)
                                }
                                pop()
                            } else {
                                append(match.value)
                            }
                            lastIndex = match.range.last + 1
                        }
                        append(m.message.substring(lastIndex))
                    }

                    //Show messages
                    if (m.sender == loggedUser.userNickname) {                                                 //Sent message: align it to the right
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
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
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                            ) {
                                ClickableText(
                                    text = annotatedString,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 18.sp,
                                        textAlign = TextAlign.Left
                                    ),
                                    onClick = { offset ->
                                        annotatedString.getStringAnnotations(
                                            tag = "USER_TAG",
                                            start = offset,
                                            end = offset
                                        )
                                            .firstOrNull()?.let { annotation ->
                                                showUserInformationPane(annotation.item)
                                            }

                                        annotatedString.getStringAnnotations(
                                            tag = "URL",
                                            start = offset,
                                            end = offset
                                        )
                                            .firstOrNull()?.let { annotation ->
                                                val urlIntent = Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse(annotation.item)
                                                )
                                                context.startActivity(urlIntent)
                                            }
                                    }
                                )
                                Text(
                                    text = "${m.date} - ${m.time}",
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
                                    .width(IntrinsicSize.Max)
                                    .widthIn(0.dp, 320.dp)
                                    .clickable(onClick = {
                                        showUserInformationPane(loggedUser.userNickname)
                                    }),
                                horizontalAlignment = Alignment.Start

                            ) {
                                CreateImage(
                                    photo = if (loggedUser.userImage == null) null else loggedUser.userImage!!.toUri(),
                                    name = "${loggedUser.userFirstName} ${loggedUser.userLastName}",
                                    size = 30
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))

                        }
                    } else {                                                                          //Received message: align it to the left
                        var user = User()
                        db.collection("Users").document(m.sender)
                            .addSnapshotListener { doc, e ->
                                if (e == null) {
                                    user = doc!!.toObject(User::class.java)!!
                                } else {
                                    Log.e("Firestore", "Error getting User data", e)
                                }
                            }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(
                                modifier = Modifier
                                    .clickable(onClick = {
                                        showUserInformationPane(m.sender)
                                    }),
                                horizontalAlignment = Alignment.End
                            ) {
                                CreateImage(
                                    photo = if (user.userImage == null) null else user.userImage!!.toUri(),
                                    name = "${user.userFirstName} ${user.userLastName}",
                                    size = 30
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(
                                horizontalAlignment = Alignment.Start,
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
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                            ) {
                                Text(
                                    text = m.sender,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    modifier = Modifier.clickable(onClick = {
                                        showUserInformationPane(m.sender)
                                    })
                                )

                                ClickableText(
                                    text = annotatedString,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 18.sp,
                                        textAlign = TextAlign.Left
                                    ),
                                    onClick = { offset ->
                                        annotatedString.getStringAnnotations(
                                            tag = "USER_TAG",
                                            start = offset,
                                            end = offset
                                        )
                                            .firstOrNull()?.let { annotation ->
                                                showUserInformationPane(annotation.item)
                                            }

                                        annotatedString.getStringAnnotations(
                                            tag = "URL",
                                            start = offset,
                                            end = offset
                                        )
                                            .firstOrNull()?.let { annotation ->
                                                val urlIntent = Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse(annotation.item)
                                                )
                                                context.startActivity(urlIntent)
                                            }
                                    }
                                )

                                Text(
                                    text = "${m.date} - ${m.time}",
                                    modifier = Modifier.padding(top = 3.dp),
                                    fontSize = 15.sp,
                                    color = Color.DarkGray
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))

                        }

                    }
                    Spacer(modifier = Modifier.height(10.dp))
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
                        //.align(Alignment.TopEnd)
                        .offset(10.dp, 0.dp)
                ) {
                    Text(buttonText, fontSize = 25.sp)
                }
            }
        }
    }
}

@Composable
fun ShowTeamUserList(
    selectedTeam: Team,
    messageValue: String,
    setMessage: (String) -> Unit,
    db: FirebaseFirestore
) {
    val teamUsers = mutableListOf<TeamMember>()
    for (tk in selectedTeam.teamMembers) {
        db.collection("Team").document(tk).get()
            .addOnSuccessListener { d ->
                teamUsers.add(d.toObject(TeamMember::class.java)!!)
            }
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 5.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(10.dp))
    ) {
        for (u in teamUsers) {
            if (u.user != loggedUser.userNickname) {
                var user = User()
                db.collection("Users").document(u.user)
                    .addSnapshotListener {
                            doc, e ->
                        if(e == null) {
                            user = doc!!.toObject(User::class.java)!!
                        }
                        else {
                            Log.e("Firestore", "Error getting User data", e)
                        }
                    }
                Row(
                    Modifier.padding(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CreateImage(
                        photo = if(user.userImage == null) null else user.userImage!!.toUri(),
                        name = "${user.userFirstName} ${user.userLastName}",
                        size = 30
                    )

                    Spacer(modifier = Modifier.width(7.dp))

                    Text(
                        text = u.user,
                        modifier = Modifier
                            .clickable {
                                setMessage("${messageValue}${u.user.removePrefix("@")}")
                            }
                            .fillMaxWidth()
                        //.border(1.dp, Color.Gray, RoundedCornerShape(10.dp))
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}